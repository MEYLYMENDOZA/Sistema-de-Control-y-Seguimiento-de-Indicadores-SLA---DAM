using Microsoft.AspNetCore.Mvc;
using Proyecto01.CORE.Core.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace Proyecto01.API.Controllers
{
    [Route("api/sla")]
    [ApiController]
    public class SlaController : ControllerBase
    {
        private readonly DbContext _context; // Cambia esto por tu DbContext específico

        public SlaController(DbContext context) // Cambia DbContext por el nombre de tu DbContext
        {
            _context = context;
        }

        /// <summary>
        /// Obtiene solicitudes con sus datos SLA para que Android calcule estadísticas
        /// </summary>
        [HttpGet("solicitudes")]
        public async Task<IActionResult> GetSolicitudes(
            [FromQuery] int meses = 12,
            [FromQuery] int? anio = null,
            [FromQuery] int? idArea = null)
        {
            try
            {
                var fechaInicio = DateTime.Now.AddMonths(-meses);

                // NOTA: Ajusta los nombres de las tablas según tu DbContext
                // Ejemplo: _context.Solicitud, _context.ConfigSla

                var solicitudes = await _context.Set<dynamic>() // Cambia 'dynamic' por tu entidad Solicitud
                    .Where(s => s.FechaSolicitud >= fechaInicio)
                    .Select(s => new
                    {
                        idSolicitud = s.IdSolicitud,
                        fechaSolicitud = s.FechaSolicitud,
                        numDiasSla = s.NumDiasSla,
                        diasUmbral = s.ConfigSla.DiasUmbral, // Ajusta según tu relación
                        idArea = s.IdArea,
                        codigoSla = s.ConfigSla.CodigoSla
                    })
                    .ToListAsync();

                return Ok(solicitudes);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new
                {
                    error = "Error al obtener solicitudes",
                    details = ex.Message
                });
            }
        }
    }
}

