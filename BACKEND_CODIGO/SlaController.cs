using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Proyecto01.CORE.Infrastructure.Data;

namespace Proyecto01.API.Controllers
{
    /// <summary>
    /// Controlador para endpoints de predicción SLA consumidos por la app Android
    /// </summary>
    [Route("api/[controller]")]
    [ApiController]
    public class SlaController : ControllerBase
    {
        private readonly Proyecto01DbContext _context;

        public SlaController(Proyecto01DbContext context)
        {
            _context = context;
        }

        /// <summary>
        /// Obtiene solicitudes con datos SLA para cálculo de predicción
        /// GET /api/sla/solicitudes
        /// </summary>
        /// <param name="meses">Número de meses hacia atrás desde hoy (por defecto 12)</param>
        /// <param name="anio">Año específico (opcional, si se usa ignora 'meses')</param>
        /// <param name="mes">Mes específico 1-12 (opcional, requiere 'anio')</param>
        /// <param name="idArea">Filtrar por área específica (opcional)</param>
        /// <returns>Lista de solicitudes con datos para calcular cumplimiento SLA</returns>
        [HttpGet("solicitudes")]
        public async Task<IActionResult> GetSolicitudes(
            [FromQuery] int? meses = 12,
            [FromQuery] int? anio = null,
            [FromQuery] int? mes = null,
            [FromQuery] int? idArea = null)
        {
            try
            {
                // Determinar fechaInicio según filtros
                DateTime? fechaInicio = null;
                if (meses.HasValue && !anio.HasValue)
                {
                    fechaInicio = DateTime.UtcNow.AddMonths(-meses.Value);
                }

                // Hacer JOIN explícito en lugar de usar Include() para mayor control
                // Intentar con nombres en singular (ajustar según tu DbContext)
                var query = from s in _context.Solicitudes.AsNoTracking()
                            join c in _context.Set<ConfigSla>().AsNoTracking() on s.IdSla equals c.IdSla into configGrp
                            from config in configGrp.DefaultIfEmpty()
                            join r in _context.Set<RolRegistro>().AsNoTracking() on s.IdRolRegistro equals r.IdRolRegistro into rolGrp
                            from rol in rolGrp.DefaultIfEmpty()
                            where s.FechaSolicitud.HasValue
                            select new { s, config, rol };

                // Aplicar filtros de fecha
                if (anio.HasValue && mes.HasValue)
                {
                    query = query.Where(x => x.s.FechaSolicitud.Value.Year == anio.Value &&
                                             x.s.FechaSolicitud.Value.Month == mes.Value);
                }
                else if (anio.HasValue)
                {
                    query = query.Where(x => x.s.FechaSolicitud.Value.Year == anio.Value);
                }
                else if (fechaInicio.HasValue)
                {
                    query = query.Where(x => x.s.FechaSolicitud.Value >= fechaInicio.Value);
                }

                // Filtrar por área si se especifica
                if (idArea.HasValue)
                {
                    query = query.Where(x => x.s.IdArea == idArea.Value);
                }

                // Proyectar a DTO que Android espera
                var solicitudes = await query
                    .OrderBy(x => x.s.FechaSolicitud)
                    .Select(x => new
                    {
                        idSolicitud = x.s.IdSolicitud,
                        // Formato ISO para Android (yyyy-MM-ddTHH:mm:ss)
                        fechaSolicitud = x.s.FechaSolicitud.HasValue
                            ? x.s.FechaSolicitud.Value.ToString("yyyy-MM-ddTHH:mm:ss")
                            : DateTime.UtcNow.ToString("yyyy-MM-ddTHH:mm:ss"),
                        fechaIngreso = x.s.FechaIngreso.HasValue
                            ? x.s.FechaIngreso.Value.ToString("yyyy-MM-ddTHH:mm:ss")
                            : null,
                        resumenSla = x.s.ResumenSla,
                        // Días que tomó resolver la solicitud
                        numDiasSla = x.s.NumDiasSla ??
                            (x.s.FechaSolicitud.HasValue && x.s.FechaIngreso.HasValue
                                ? (int)(x.s.FechaIngreso.Value - x.s.FechaSolicitud.Value).TotalDays
                                : 0),
                        // Días máximos permitidos según configuración SLA
                        diasUmbral = x.config != null ? (x.config.DiasUmbral ?? 30) : 30,
                        idArea = x.s.IdArea,
                        codigoSla = x.config != null ? x.config.CodigoSla : "N/A",
                        // Rol del registro - Mapear directamente desde el JOIN
                        rolRegistro = x.rol != null ? new
                        {
                            nombre = x.rol.NombreRol ?? "Sin Nombre"
                        } : new
                        {
                            nombre = "Sin Rol Asignado"
                        }
                    })
                    .ToListAsync();

                // Log para debugging
                Console.WriteLine($"[SlaController] Solicitudes encontradas: {solicitudes.Count}");
                Console.WriteLine($"[SlaController] Filtros: meses={meses}, anio={anio}, mes={mes}, idArea={idArea}");

                // Verificar si los roles se están cargando
                var conRol = solicitudes.Count(s => s.rolRegistro != null);
                var sinRol = solicitudes.Count(s => s.rolRegistro == null);
                Console.WriteLine($"[SlaController] Con rol: {conRol}, Sin rol: {sinRol}");

                // Mostrar primeros 3 registros para depuración
                if (solicitudes.Any())
                {
                    Console.WriteLine($"[SlaController] Muestra de primeros 3 registros:");
                    foreach (var sol in solicitudes.Take(3))
                    {
                        Console.WriteLine($"  ID={sol.idSolicitud}, Rol={sol.rolRegistro?.nombre ?? "NULL"}, CodigoSLA={sol.codigoSla}");
                    }
                }

                return Ok(solicitudes);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[SlaController] ERROR: {ex.Message}");
                return StatusCode(500, new
                {
                    error = "Error al obtener solicitudes",
                    detail = ex.Message,
                    stackTrace = ex.StackTrace
                });
            }
        }


        /// <summary>
        /// Endpoint de prueba para verificar conectividad
        /// GET /api/sla/ping
        /// </summary>
        [HttpGet("ping")]
        public IActionResult Ping()
        {
            return Ok(new
            {
                status = "online",
                message = "SLA Controller funcionando correctamente",
                timestamp = DateTime.UtcNow.ToString("yyyy-MM-ddTHH:mm:ss"),
                version = "1.0"
            });
        }

        /// <summary>
        /// Obtiene estadísticas básicas de SLA (opcional, para debugging)
        /// GET /api/sla/estadisticas
        /// </summary>
        [HttpGet("estadisticas")]
        public async Task<IActionResult> GetEstadisticas([FromQuery] int meses = 12)
        {
            try
            {
                var fechaInicio = DateTime.UtcNow.AddMonths(-meses);

                var total = await _context.Solicitudes
                    .Where(s => s.FechaSolicitud.HasValue && s.FechaSolicitud.Value >= fechaInicio)
                    .CountAsync();

                var cumplidas = await _context.Solicitudes
                    .Include(s => s.ConfigSla)
                    .Where(s => s.FechaSolicitud.HasValue &&
                               s.FechaSolicitud.Value >= fechaInicio &&
                               s.NumDiasSla.HasValue &&
                               s.ConfigSla != null &&
                               s.NumDiasSla.Value <= s.ConfigSla.DiasUmbral)
                    .CountAsync();

                var porcentaje = total > 0 ? (cumplidas * 100.0 / total) : 0;

                return Ok(new
                {
                    periodoMeses = meses,
                    totalSolicitudes = total,
                    solicitudesCumplidas = cumplidas,
                    solicitudesIncumplidas = total - cumplidas,
                    porcentajeCumplimiento = Math.Round(porcentaje, 2)
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { error = ex.Message });
            }
        }

        /// <summary>
        /// Obtiene los años disponibles en la base de datos
        /// GET /api/sla/años-disponibles
        /// </summary>
        [HttpGet("años-disponibles")]
        public async Task<IActionResult> GetAñosDisponibles()
        {
            try
            {
                var años = await _context.Solicitudes
                    .Where(s => s.FechaSolicitud.HasValue)
                    .Select(s => s.FechaSolicitud.Value.Year)
                    .Distinct()
                    .OrderByDescending(y => y)
                    .ToListAsync();

                return Ok(años);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { error = ex.Message });
            }
        }

        /// <summary>
        /// Obtiene los meses disponibles para un año específico
        /// GET /api/sla/meses-disponibles?anio=2024
        /// </summary>
        [HttpGet("meses-disponibles")]
        public async Task<IActionResult> GetMesesDisponibles([FromQuery] int anio)
        {
            try
            {
                var meses = await _context.Solicitudes
                    .Where(s => s.FechaSolicitud.HasValue && s.FechaSolicitud.Value.Year == anio)
                    .Select(s => s.FechaSolicitud.Value.Month)
                    .Distinct()
                    .OrderBy(m => m)
                    .ToListAsync();

                return Ok(meses);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { error = ex.Message });
            }
        }
    }
}

