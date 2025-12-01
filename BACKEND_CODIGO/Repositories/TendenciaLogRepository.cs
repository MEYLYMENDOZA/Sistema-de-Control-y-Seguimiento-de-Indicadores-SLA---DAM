using Microsoft.EntityFrameworkCore;
using Proyecto01.CORE.Domain.Entities;
using Proyecto01.CORE.Infrastructure.Data;
using System.Linq;
using System.Threading.Tasks;

namespace Proyecto01.CORE.Application.Repositories
{
    /// <summary>
    /// Implementación del repositorio de logs de tendencia
    /// US-12: Auditoría de reportes
    /// </summary>
    public class TendenciaLogRepository : ITendenciaLogRepository
    {
        private readonly Proyecto01DbContext _context;

        public TendenciaLogRepository(Proyecto01DbContext context)
        {
            _context = context;
        }

        public async Task<PrediccionTendenciaLog> Guardar(PrediccionTendenciaLog log)
        {
            _context.PrediccionTendenciaLogs.Add(log);
            await _context.SaveChangesAsync();
            return log;
        }

        public async Task<PrediccionTendenciaLog?> ObtenerUltimo()
        {
            return await _context.PrediccionTendenciaLogs
                .OrderByDescending(l => l.FechaGeneracion)
                .FirstOrDefaultAsync();
        }
    }
}

