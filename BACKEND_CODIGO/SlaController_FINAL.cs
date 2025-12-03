using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Proyecto01.CORE.Infrastructure.Data;

namespace Proyecto01.API.Controllers
{
    [Route("api/sla")]
    [ApiController]
    public class SlaController : ControllerBase
    {
        private readonly Proyecto01DbContext _context;

        public SlaController(Proyecto01DbContext context)
        {
            _context = context;
        }

        /// <summary>
        /// Obtiene solicitudes con sus datos SLA para que Android calcule estadísticas
        /// </summary>
        /// <param name="meses">Número de meses históricos a obtener (default: 12)</param>
        /// <param name="anio">Año específico (opcional)</param>
        /// <param name="idArea">Filtrar por área específica (opcional)</param>
        /// <returns>Lista de solicitudes con información de SLA</returns>
        [HttpGet("solicitudes")]
        public async Task<IActionResult> GetSolicitudes(
            [FromQuery] int meses = 12,
            [FromQuery] int? anio = null,
            [FromQuery] int? idArea = null)
        {
            try
            {
                // Calcular fecha de inicio según los meses solicitados
                var fechaInicio = DateTime.Now.AddMonths(-meses);

                // Query base
                var query = _context.Solicitudes
                    .Include(s => s.ConfigSla)
                    .AsQueryable();

                // Aplicar filtro de fecha
                query = query.Where(s => s.FechaSolicitud >= fechaInicio);

                // Aplicar filtro de año si se especifica
                if (anio.HasValue)
                {
                    query = query.Where(s => s.FechaSolicitud.HasValue && s.FechaSolicitud.Value.Year == anio.Value);
                }

                // Aplicar filtro de área si se especifica
                if (idArea.HasValue)
                {
                    query = query.Where(s => s.IdArea == idArea.Value);
                }

                // Seleccionar solo los campos necesarios para Android
                var solicitudes = await query
                    .OrderBy(s => s.FechaSolicitud)
                    .Select(s => new
                    {
                        idSolicitud = s.IdSolicitud,
                        fechaSolicitud = s.FechaSolicitud,
                        numDiasSla = s.NumDiasSla ?? 0,
                        diasUmbral = s.ConfigSla != null ? s.ConfigSla.DiasUmbral ?? 5 : 5,
                        idArea = s.IdArea,
                        codigoSla = s.ConfigSla != null ? s.ConfigSla.CodigoSla : "SLA_DESCONOCIDO"
                    })
                    .ToListAsync();

                return Ok(solicitudes);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new
                {
                    error = "Error al obtener solicitudes",
                    message = ex.Message,
                    details = ex.InnerException?.Message
                });
            }
        }

        /// <summary>
        /// Endpoint de salud para verificar que el servicio está funcionando
        /// </summary>
        [HttpGet("health")]
        public IActionResult Health()
        {
            return Ok(new
            {
                status = "OK",
                message = "SLA API está funcionando correctamente",
                timestamp = DateTime.Now,
                database = _context.Database.CanConnect() ? "Conectada" : "Desconectada"
            });
        }

        /// <summary>
        /// Obtiene estadísticas generales de SLA
        /// </summary>
        [HttpGet("estadisticas")]
        public async Task<IActionResult> GetEstadisticas([FromQuery] int meses = 12)
        {
            try
            {
                var fechaInicio = DateTime.Now.AddMonths(-meses);

                var solicitudes = await _context.Solicitudes
                    .Include(s => s.ConfigSla)
                    .Where(s => s.FechaSolicitud >= fechaInicio)
                    .ToListAsync();

                var total = solicitudes.Count;
                var cumplidas = solicitudes.Count(s =>
                    s.NumDiasSla.HasValue &&
                    s.ConfigSla != null &&
                    s.ConfigSla.DiasUmbral.HasValue &&
                    s.NumDiasSla.Value <= s.ConfigSla.DiasUmbral.Value);

                var incumplidas = total - cumplidas;
                var porcentajeCumplimiento = total > 0 ? (cumplidas * 100.0) / total : 0;

                return Ok(new
                {
                    totalSolicitudes = total,
                    solicitudesCumplidas = cumplidas,
                    solicitudesIncumplidas = incumplidas,
                    porcentajeCumplimiento = Math.Round(porcentajeCumplimiento, 2),
                    periodo = $"Últimos {meses} meses",
                    fechaInicio = fechaInicio.ToString("yyyy-MM-dd"),
                    fechaFin = DateTime.Now.ToString("yyyy-MM-dd")
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new
                {
                    error = "Error al obtener estadísticas",
                    message = ex.Message
                });
            }
        }

        /// <summary>
        /// Obtiene las áreas disponibles
        /// </summary>
        [HttpGet("areas")]
        public async Task<IActionResult> GetAreas()
        {
            try
            {
                var areas = await _context.Areas
                    .Select(a => new
                    {
                        idArea = a.IdArea,
                        nombreArea = a.NombreArea,
                        descripcion = a.Descripcion
                    })
                    .ToListAsync();

                return Ok(areas);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new
                {
                    error = "Error al obtener áreas",
                    message = ex.Message
                });
            }
        }
    }
}

