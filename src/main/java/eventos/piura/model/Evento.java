package eventos.piura.model;

public class Evento {
    private Long id;
    private String titulo;
    private String descripcion;
    private String fecha;

    public Evento(Long id, String titulo, String descripcion, String fecha) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    // getters
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getFecha() { return fecha; }
}
