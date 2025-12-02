// UserController.cs
// Controlador para gestión de usuarios
// Este archivo debe estar en: Proyecto01.API/Controllers/UserController.cs

using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Proyecto01.API.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class UserController : ControllerBase
    {
        private readonly ApplicationDbContext _context;

        public UserController(ApplicationDbContext context)
        {
            _context = context;
        }

        // GET: api/User
        // Obtiene todos los usuarios con información de personal
        [HttpGet]
        public async Task<ActionResult<IEnumerable<UsuarioResponseDTO>>> GetAllUsuarios()
        {
            try
            {
                var usuarios = await _context.usuario
                    .Include(u => u.Personal)
                    .Include(u => u.RolSistema)
                    .Include(u => u.EstadoUsuario)
                    .Select(u => new UsuarioResponseDTO
                    {
                        IdUsuario = u.id_usuario,
                        Username = u.username,
                        Correo = u.correo,
                        IdRolSistema = u.id_rol_sistema,
                        RolNombre = u.RolSistema.nombre,
                        IdEstadoUsuario = u.id_estado_usuario,
                        EstadoNombre = u.EstadoUsuario.descripcion,
                        CreadoEn = u.creado_en,
                        UltimoLogin = u.ultimo_login,
                        Personal = u.Personal != null ? new PersonalDTO
                        {
                            IdPersonal = u.Personal.id_personal,
                            Nombres = u.Personal.nombres,
                            Apellidos = u.Personal.apellidos,
                            Documento = u.Personal.documento,
                            Estado = u.Personal.estado
                        } : null
                    })
                    .ToListAsync();

                return Ok(usuarios);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Error al obtener usuarios", error = ex.Message });
            }
        }

        // GET: api/User/{id}
        // Obtiene un usuario por ID
        [HttpGet("{id}")]
        public async Task<ActionResult<UsuarioResponseDTO>> GetUsuarioById(int id)
        {
            try
            {
                var usuario = await _context.usuario
                    .Include(u => u.Personal)
                    .Include(u => u.RolSistema)
                    .Include(u => u.EstadoUsuario)
                    .Where(u => u.id_usuario == id)
                    .Select(u => new UsuarioResponseDTO
                    {
                        IdUsuario = u.id_usuario,
                        Username = u.username,
                        Correo = u.correo,
                        IdRolSistema = u.id_rol_sistema,
                        RolNombre = u.RolSistema.nombre,
                        IdEstadoUsuario = u.id_estado_usuario,
                        EstadoNombre = u.EstadoUsuario.descripcion,
                        CreadoEn = u.creado_en,
                        UltimoLogin = u.ultimo_login,
                        Personal = u.Personal != null ? new PersonalDTO
                        {
                            IdPersonal = u.Personal.id_personal,
                            Nombres = u.Personal.nombres,
                            Apellidos = u.Personal.apellidos,
                            Documento = u.Personal.documento,
                            Estado = u.Personal.estado
                        } : null
                    })
                    .FirstOrDefaultAsync();

                if (usuario == null)
                {
                    return NotFound(new { message = "Usuario no encontrado" });
                }

                return Ok(usuario);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Error al obtener usuario", error = ex.Message });
            }
        }

        // POST: api/User
        // Crea un nuevo usuario
        [HttpPost]
        public async Task<ActionResult<UsuarioResponseDTO>> CreateUsuario([FromBody] CrearUsuarioDTO dto)
        {
            try
            {
                // Validar datos
                if (string.IsNullOrWhiteSpace(dto.Username) ||
                    string.IsNullOrWhiteSpace(dto.Correo) ||
                    string.IsNullOrWhiteSpace(dto.Password))
                {
                    return BadRequest(new { message = "Username, correo y contraseña son obligatorios" });
                }

                // Verificar si el username ya existe
                if (await _context.usuario.AnyAsync(u => u.username == dto.Username))
                {
                    return Conflict(new { message = "El nombre de usuario ya está registrado" });
                }

                // Verificar si el correo ya existe
                if (await _context.usuario.AnyAsync(u => u.correo == dto.Correo))
                {
                    return Conflict(new { message = "El correo ya está registrado" });
                }

                // Crear nuevo usuario
                var nuevoUsuario = new usuario
                {
                    username = dto.Username,
                    correo = dto.Correo,
                    password_hash = HashPassword(dto.Password), // Método para hashear la contraseña
                    id_rol_sistema = dto.IdRolSistema,
                    id_estado_usuario = dto.IdEstadoUsuario,
                    creado_en = DateTime.Now
                };

                _context.usuario.Add(nuevoUsuario);
                await _context.SaveChangesAsync();

                // Si se proporcionan datos de personal, crearlos
                if (!string.IsNullOrWhiteSpace(dto.Nombres) || !string.IsNullOrWhiteSpace(dto.Apellidos))
                {
                    var personal = new personal
                    {
                        id_usuario = nuevoUsuario.id_usuario,
                        nombres = dto.Nombres ?? "",
                        apellidos = dto.Apellidos ?? "",
                        documento = dto.Documento,
                        estado = "Activo",
                        creado_en = DateTime.Now
                    };

                    _context.personal.Add(personal);
                    await _context.SaveChangesAsync();
                }

                // Devolver el usuario creado
                return await GetUsuarioById(nuevoUsuario.id_usuario);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Error al crear usuario", error = ex.Message });
            }
        }

        // PUT: api/User/{id}
        // Actualiza un usuario existente
        [HttpPut("{id}")]
        public async Task<ActionResult<UsuarioResponseDTO>> UpdateUsuario(int id, [FromBody] CrearUsuarioDTO dto)
        {
            try
            {
                var usuario = await _context.usuario
                    .Include(u => u.Personal)
                    .FirstOrDefaultAsync(u => u.id_usuario == id);

                if (usuario == null)
                {
                    return NotFound(new { message = "Usuario no encontrado" });
                }

                // Actualizar datos del usuario
                usuario.correo = dto.Correo;
                usuario.id_rol_sistema = dto.IdRolSistema;
                usuario.id_estado_usuario = dto.IdEstadoUsuario;
                usuario.actualizado_en = DateTime.Now;

                // Actualizar contraseña solo si se proporciona
                if (!string.IsNullOrWhiteSpace(dto.Password) && dto.Password != "sin_cambio")
                {
                    usuario.password_hash = HashPassword(dto.Password);
                }

                // Actualizar o crear datos de personal
                if (usuario.Personal != null)
                {
                    usuario.Personal.nombres = dto.Nombres ?? "";
                    usuario.Personal.apellidos = dto.Apellidos ?? "";
                    usuario.Personal.documento = dto.Documento;
                    usuario.Personal.actualizado_en = DateTime.Now;
                }
                else if (!string.IsNullOrWhiteSpace(dto.Nombres) || !string.IsNullOrWhiteSpace(dto.Apellidos))
                {
                    var personal = new personal
                    {
                        id_usuario = usuario.id_usuario,
                        nombres = dto.Nombres ?? "",
                        apellidos = dto.Apellidos ?? "",
                        documento = dto.Documento,
                        estado = "Activo",
                        creado_en = DateTime.Now
                    };
                    _context.personal.Add(personal);
                }

                await _context.SaveChangesAsync();

                return await GetUsuarioById(id);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Error al actualizar usuario", error = ex.Message });
            }
        }

        // DELETE: api/User/{id}
        // Elimina (desactiva) un usuario
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteUsuario(int id)
        {
            try
            {
                var usuario = await _context.usuario.FindAsync(id);

                if (usuario == null)
                {
                    return NotFound(new { message = "Usuario no encontrado" });
                }

                // En lugar de eliminar, desactivar el usuario
                usuario.id_estado_usuario = 2; // Asumiendo que 2 = Inactivo
                usuario.actualizado_en = DateTime.Now;

                await _context.SaveChangesAsync();

                return Ok(new { message = "Usuario desactivado exitosamente" });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Error al desactivar usuario", error = ex.Message });
            }
        }

        // GET: api/User/roles
        // Obtiene todos los roles del sistema
        [HttpGet("roles")]
        public async Task<ActionResult<IEnumerable<RolSistemaDTO>>> GetRoles()
        {
            try
            {
                var roles = await _context.roles_sistema
                    .Where(r => r.es_activo)
                    .Select(r => new RolSistemaDTO
                    {
                        IdRolSistema = r.id_rol_sistema,
                        Codigo = r.codigo,
                        Nombre = r.nombre,
                        Descripcion = r.descripcion,
                        EsActivo = r.es_activo
                    })
                    .ToListAsync();

                return Ok(roles);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Error al obtener roles", error = ex.Message });
            }
        }

        // GET: api/User/estados
        // Obtiene todos los estados de usuario
        [HttpGet("estados")]
        public async Task<ActionResult<IEnumerable<EstadoUsuarioDTO>>> GetEstados()
        {
            try
            {
                var estados = await _context.estado_usuario_catalogo
                    .Select(e => new EstadoUsuarioDTO
                    {
                        IdEstadoUsuario = e.id_estado_usuario,
                        Codigo = e.codigo,
                        Descripcion = e.descripcion
                    })
                    .ToListAsync();

                return Ok(estados);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { message = "Error al obtener estados", error = ex.Message });
            }
        }

        // Método helper para hashear contraseñas (usando BCrypt o similar)
        private string HashPassword(string password)
        {
            // IMPORTANTE: Usar BCrypt.Net-Next en producción
            // Instalar: dotnet add package BCrypt.Net-Next
            // return BCrypt.Net.BCrypt.HashPassword(password);

            // Por ahora, retornar la contraseña hasheada con un método simple
            // ⚠️ ESTO ES SOLO PARA DESARROLLO - USA BCRYPT EN PRODUCCIÓN
            using (var sha256 = System.Security.Cryptography.SHA256.Create())
            {
                var hashedBytes = sha256.ComputeHash(System.Text.Encoding.UTF8.GetBytes(password));
                return Convert.ToBase64String(hashedBytes);
            }
        }
    }

    // DTOs
    public class UsuarioResponseDTO
    {
        public int IdUsuario { get; set; }
        public string Username { get; set; }
        public string Correo { get; set; }
        public int IdRolSistema { get; set; }
        public string? RolNombre { get; set; }
        public int IdEstadoUsuario { get; set; }
        public string? EstadoNombre { get; set; }
        public DateTime? CreadoEn { get; set; }
        public DateTime? UltimoLogin { get; set; }
        public PersonalDTO? Personal { get; set; }
    }

    public class PersonalDTO
    {
        public int IdPersonal { get; set; }
        public string? Nombres { get; set; }
        public string? Apellidos { get; set; }
        public string? Documento { get; set; }
        public string? Estado { get; set; }
    }

    public class CrearUsuarioDTO
    {
        public string Username { get; set; }
        public string Correo { get; set; }
        public string Password { get; set; }
        public int IdRolSistema { get; set; }
        public int IdEstadoUsuario { get; set; }
        public string? Nombres { get; set; }
        public string? Apellidos { get; set; }
        public string? Documento { get; set; }
        public string? Telefono { get; set; }
    }

    public class RolSistemaDTO
    {
        public int IdRolSistema { get; set; }
        public string Codigo { get; set; }
        public string Nombre { get; set; }
        public string? Descripcion { get; set; }
        public bool EsActivo { get; set; }
    }

    public class EstadoUsuarioDTO
    {
        public int IdEstadoUsuario { get; set; }
        public string Codigo { get; set; }
        public string Descripcion { get; set; }
    }
}

