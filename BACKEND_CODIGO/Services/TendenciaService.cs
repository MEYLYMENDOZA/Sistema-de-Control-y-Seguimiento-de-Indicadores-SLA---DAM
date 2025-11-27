using Proyecto01.CORE.Application.Repositories;
using Proyecto01.CORE.Domain.Entities;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Proyecto01.CORE.Application.Services
{
    /// <summary>
    /// Servicio para cálculo de tendencias y proyección SLA
    /// US-12: Visualizar tendencia y proyección de cumplimiento SLA
    /// </summary>
    public class TendenciaService
    {
        private readonly ISlaRepository _slaRepository;
        private readonly ITendenciaLogRepository _logRepository;

        // Umbrales SLA según especificación
        private const int UMBRAL_SLA1 = 35; // días
        private const int UMBRAL_SLA2 = 20; // días

        // Mínimo de meses para generar proyección confiable
        private const int MINIMO_MESES_PROYECCION = 3;

        // Tolerancia para determinar si la tendencia es estable
        private const double TOLERANCIA_ESTABLE = 0.5; // ±0.5% por mes

        public TendenciaService(ISlaRepository slaRepository, ITendenciaLogRepository logRepository)
        {
            _slaRepository = slaRepository;
            _logRepository = logRepository;
        }

        /// <summary>
        /// Genera el reporte completo de tendencia y proyección
        /// </summary>
        public async Task<TendenciaReporteDto> GenerarReporteTendencia(
            int? mes,
            int? anio,
            string tipoSla,
            int? idArea,
            string? usuarioSolicitante = null,
            string? ipCliente = null)
        {
            // 1. Validar parámetros (OWASP - Sanitización)
            ValidarParametros(mes, anio, tipoSla, idArea);

            // 2. Calcular rango de fechas (últimos 12 meses hasta el mes/año especificado)
            var (fechaInicio, fechaFin) = CalcularRangoFechas(mes, anio);

            // 3. Obtener solicitudes desde la base de datos
            var solicitudes = await _slaRepository.ObtenerSolicitudesPorRango(
                fechaInicio,
                fechaFin,
                tipoSla,
                idArea);

            // 4. Calcular cumplimiento SLA mensual (histórico)
            var historicoMensual = CalcularCumplimientoMensual(solicitudes, tipoSla);

            // 5. Validar que hay suficientes datos
            if (historicoMensual.Count < MINIMO_MESES_PROYECCION)
            {
                throw new InvalidOperationException(
                    $"No es posible generar proyección. Se requieren al menos {MINIMO_MESES_PROYECCION} meses de datos. Datos disponibles: {historicoMensual.Count}");
            }

            // 6. Calcular regresión lineal (tendencia)
            var (pendiente, intercepto) = CalcularRegresionLineal(historicoMensual);

            // 7. Generar línea de tendencia
            var lineaTendencia = GenerarLineaTendencia(historicoMensual, pendiente, intercepto);

            // 8. Calcular proyección para el próximo mes
            var proximoMes = historicoMensual.Count + 1;
            var proyeccion = Math.Round(pendiente * proximoMes + intercepto, 2);

            // 9. Determinar estado de tendencia
            var estadoTendencia = DeterminarEstadoTendencia(pendiente);

            // 10. Registrar en log de auditoría
            await GuardarLogAuditoria(
                mes, anio, tipoSla, idArea,
                proyeccion, pendiente, intercepto,
                estadoTendencia, historicoMensual.Count,
                usuarioSolicitante, ipCliente);

            // 11. Construir respuesta
            return new TendenciaReporteDto
            {
                Historico = historicoMensual,
                Tendencia = lineaTendencia,
                Proyeccion = proyeccion,
                Pendiente = pendiente,
                Intercepto = intercepto,
                EstadoTendencia = estadoTendencia,
                TotalRegistros = solicitudes.Count,
                FechaGeneracion = DateTime.UtcNow
            };
        }

        /// <summary>
        /// Valida y sanitiza los parámetros de entrada (OWASP)
        /// </summary>
        private void ValidarParametros(int? mes, int? anio, string tipoSla, int? idArea)
        {
            // Validar mes (1-12)
            if (mes.HasValue && (mes.Value < 1 || mes.Value > 12))
            {
                throw new ArgumentException("El mes debe estar entre 1 y 12", nameof(mes));
            }

            // Validar año (rango razonable)
            if (anio.HasValue && (anio.Value < 2000 || anio.Value > DateTime.Now.Year + 1))
            {
                throw new ArgumentException($"El año debe estar entre 2000 y {DateTime.Now.Year + 1}", nameof(anio));
            }

            // Validar tipo SLA (whitelist)
            var tiposSlaPermitidos = new[] { "SLA1", "SLA2" };
            if (!string.IsNullOrEmpty(tipoSla) && !tiposSlaPermitidos.Contains(tipoSla.ToUpper()))
            {
                throw new ArgumentException("El tipo de SLA debe ser SLA1 o SLA2", nameof(tipoSla));
            }

            // Validar idArea (debe ser positivo)
            if (idArea.HasValue && idArea.Value <= 0)
            {
                throw new ArgumentException("El ID de área debe ser un número positivo", nameof(idArea));
            }
        }

        /// <summary>
        /// Calcula el rango de fechas para el análisis
        /// </summary>
        private (DateTime fechaInicio, DateTime fechaFin) CalcularRangoFechas(int? mes, int? anio)
        {
            DateTime fechaFin;

            if (anio.HasValue && mes.HasValue)
            {
                // Fecha fin: último día del mes especificado
                fechaFin = new DateTime(anio.Value, mes.Value, DateTime.DaysInMonth(anio.Value, mes.Value))
                    .Date.AddDays(1).AddSeconds(-1);
            }
            else if (anio.HasValue)
            {
                // Fecha fin: último día del año especificado
                fechaFin = new DateTime(anio.Value, 12, 31).Date.AddDays(1).AddSeconds(-1);
            }
            else
            {
                // Fecha fin: hoy
                fechaFin = DateTime.Today.AddDays(1).AddSeconds(-1);
            }

            // Fecha inicio: 12 meses antes de la fecha fin
            var fechaInicio = new DateTime(fechaFin.Year, fechaFin.Month, 1).AddMonths(-11);

            return (fechaInicio, fechaFin);
        }

        /// <summary>
        /// Calcula el % de cumplimiento SLA por mes
        /// </summary>
        private List<PuntoHistoricoDto> CalcularCumplimientoMensual(
            List<SlaRegistro> solicitudes,
            string tipoSla)
        {
            var umbral = tipoSla.ToUpper() == "SLA1" ? UMBRAL_SLA1 : UMBRAL_SLA2;

            var agrupado = solicitudes
                .Where(s => s.FechaSolicitud.HasValue && s.FechaIngreso.HasValue)
                .GroupBy(s => new
                {
                    Anio = s.FechaSolicitud!.Value.Year,
                    Mes = s.FechaSolicitud!.Value.Month
                })
                .OrderBy(g => g.Key.Anio)
                .ThenBy(g => g.Key.Mes)
                .Select((grupo, index) =>
                {
                    var totalCasos = grupo.Count();
                    var cumplidos = grupo.Count(s =>
                    {
                        var diasTranscurridos = (s.FechaIngreso!.Value - s.FechaSolicitud!.Value).TotalDays;
                        return diasTranscurridos < umbral;
                    });

                    var porcentajeCumplimiento = totalCasos > 0
                        ? Math.Round((double)cumplidos / totalCasos * 100, 2)
                        : 0;

                    return new PuntoHistoricoDto
                    {
                        Mes = $"{ObtenerNombreMes(grupo.Key.Mes)} {grupo.Key.Anio}",
                        Valor = porcentajeCumplimiento,
                        Orden = index + 1,
                        TotalCasos = totalCasos,
                        Cumplidos = cumplidos,
                        NoCumplidos = totalCasos - cumplidos
                    };
                })
                .ToList();

            return agrupado;
        }

        /// <summary>
        /// Calcula la regresión lineal (método de mínimos cuadrados)
        /// y = mx + b
        /// </summary>
        private (double pendiente, double intercepto) CalcularRegresionLineal(List<PuntoHistoricoDto> datos)
        {
            int n = datos.Count;
            double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

            for (int i = 0; i < n; i++)
            {
                double x = datos[i].Orden;
                double y = datos[i].Valor;

                sumX += x;
                sumY += y;
                sumXY += x * y;
                sumX2 += x * x;
            }

            // Fórmulas de regresión lineal
            double pendiente = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
            double intercepto = (sumY - pendiente * sumX) / n;

            return (Math.Round(pendiente, 6), Math.Round(intercepto, 6));
        }

        /// <summary>
        /// Genera los puntos de la línea de tendencia
        /// </summary>
        private List<PuntoTendenciaDto> GenerarLineaTendencia(
            List<PuntoHistoricoDto> historico,
            double pendiente,
            double intercepto)
        {
            return historico.Select(h => new PuntoTendenciaDto
            {
                Mes = h.Mes,
                Valor = Math.Round(pendiente * h.Orden + intercepto, 2),
                Orden = h.Orden
            }).ToList();
        }

        /// <summary>
        /// Determina el estado de la tendencia
        /// </summary>
        private string DeterminarEstadoTendencia(double pendiente)
        {
            if (pendiente > TOLERANCIA_ESTABLE)
            {
                return "positiva";
            }
            else if (pendiente < -TOLERANCIA_ESTABLE)
            {
                return "negativa";
            }
            else
            {
                return "estable";
            }
        }

        /// <summary>
        /// Guarda el log de auditoría
        /// </summary>
        private async Task GuardarLogAuditoria(
            int? mes, int? anio, string tipoSla, int? idArea,
            double proyeccion, double pendiente, double intercepto,
            string estadoTendencia, int totalRegistros,
            string? usuarioSolicitante, string? ipCliente)
        {
            var log = new PrediccionTendenciaLog
            {
                FechaGeneracion = DateTime.UtcNow,
                UsuarioSolicitante = usuarioSolicitante,
                TipoSla = tipoSla?.ToUpper() ?? "SLA1",
                Mes = mes,
                Anio = anio,
                IdArea = idArea,
                Prediccion = (decimal)proyeccion,
                Pendiente = (decimal)pendiente,
                Intercepto = (decimal)intercepto,
                EstadoTendencia = estadoTendencia,
                TotalRegistros = totalRegistros,
                IpCliente = ipCliente,
                Observaciones = $"Reporte generado exitosamente con {totalRegistros} registros"
            };

            await _logRepository.Guardar(log);
        }

        /// <summary>
        /// Convierte número de mes a nombre
        /// </summary>
        private string ObtenerNombreMes(int mes)
        {
            var nombres = new[]
            {
                "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
            };
            return nombres[mes - 1];
        }
    }

    // ===== DTOs DE RESPUESTA =====

    public class TendenciaReporteDto
    {
        public List<PuntoHistoricoDto> Historico { get; set; } = new();
        public List<PuntoTendenciaDto> Tendencia { get; set; } = new();
        public double Proyeccion { get; set; }
        public double Pendiente { get; set; }
        public double Intercepto { get; set; }
        public string EstadoTendencia { get; set; } = "estable";
        public int TotalRegistros { get; set; }
        public DateTime FechaGeneracion { get; set; }
    }

    public class PuntoHistoricoDto
    {
        public string Mes { get; set; } = "";
        public double Valor { get; set; }
        public int Orden { get; set; }
        public int TotalCasos { get; set; }
        public int Cumplidos { get; set; }
        public int NoCumplidos { get; set; }
    }

    public class PuntoTendenciaDto
    {
        public string Mes { get; set; } = "";
        public double Valor { get; set; }
        public int Orden { get; set; }
    }
}

