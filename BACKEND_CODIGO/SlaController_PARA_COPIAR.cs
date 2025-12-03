using Microsoft.AspNetCore.Mvc;

namespace Proyecto01.API.Controllers
{
    [Route("api/sla")]
    [ApiController]
    public class SlaController : ControllerBase
    {
        /// <summary>
        /// Obtiene solicitudes para que Android calcule estadísticas de SLA
        /// DATOS DE PRUEBA - Reemplazar con datos reales después
        /// </summary>
        [HttpGet("solicitudes")]
        public IActionResult GetSolicitudes([FromQuery] int meses = 12)
        {
            // Datos de prueba temporales
            // Simulan solicitudes de los últimos meses
            var datosPrueba = new[]
            {
                new
                {
                    idSolicitud = 1,
                    fechaSolicitud = "2024-11-01T09:00:00",
                    numDiasSla = 3,
                    diasUmbral = 5,
                    idArea = 1,
                    codigoSla = "SLA_NORMAL"
                },
                new
                {
                    idSolicitud = 2,
                    fechaSolicitud = "2024-11-05T10:30:00",
                    numDiasSla = 4,
                    diasUmbral = 5,
                    idArea = 1,
                    codigoSla = "SLA_NORMAL"
                },
                new
                {
                    idSolicitud = 3,
                    fechaSolicitud = "2024-11-10T14:00:00",
                    numDiasSla = 7,
                    diasUmbral = 5,
                    idArea = 2,
                    codigoSla = "SLA_URGENTE"
                },
                new
                {
                    idSolicitud = 4,
                    fechaSolicitud = "2024-11-15T11:20:00",
                    numDiasSla = 2,
                    diasUmbral = 5,
                    idArea = 1,
                    codigoSla = "SLA_NORMAL"
                },
                new
                {
                    idSolicitud = 5,
                    fechaSolicitud = "2024-11-20T16:45:00",
                    numDiasSla = 5,
                    diasUmbral = 5,
                    idArea = 2,
                    codigoSla = "SLA_NORMAL"
                },
                new
                {
                    idSolicitud = 6,
                    fechaSolicitud = "2024-10-01T08:30:00",
                    numDiasSla = 3,
                    diasUmbral = 5,
                    idArea = 1,
                    codigoSla = "SLA_NORMAL"
                },
                new
                {
                    idSolicitud = 7,
                    fechaSolicitud = "2024-10-15T12:00:00",
                    numDiasSla = 6,
                    diasUmbral = 5,
                    idArea = 1,
                    codigoSla = "SLA_NORMAL"
                },
                new
                {
                    idSolicitud = 8,
                    fechaSolicitud = "2024-09-10T10:00:00",
                    numDiasSla = 4,
                    diasUmbral = 5,
                    idArea = 2,
                    codigoSla = "SLA_NORMAL"
                },
                new
                {
                    idSolicitud = 9,
                    fechaSolicitud = "2024-09-20T15:30:00",
                    numDiasSla = 8,
                    diasUmbral = 5,
                    idArea = 1,
                    codigoSla = "SLA_URGENTE"
                },
                new
                {
                    idSolicitud = 10,
                    fechaSolicitud = "2024-08-15T09:15:00",
                    numDiasSla = 3,
                    diasUmbral = 5,
                    idArea = 1,
                    codigoSla = "SLA_NORMAL"
                }
            };

            return Ok(datosPrueba);
        }

        /// <summary>
        /// Health check para verificar que el endpoint está disponible
        /// </summary>
        [HttpGet("health")]
        public IActionResult Health()
        {
            return Ok(new
            {
                status = "OK",
                message = "SLA API está funcionando correctamente",
                timestamp = DateTime.Now
            });
        }
    }
}

