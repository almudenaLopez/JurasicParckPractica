package parquejurasico;

public class EventoCriticoExcepcion extends Exception{
    private static final long serialVersionUID = 1L;

    public EventoCriticoExcepcion(String mensaje) {
        super(mensaje);
    }
}