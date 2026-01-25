package ${package}.module.auth.dto;

public class UserDto {
  public Long id;
  public String username;
  public String password; // solo per uso interno
  public String ruolo; // ADMIN, OPERATORE
  public boolean attivo;
}
