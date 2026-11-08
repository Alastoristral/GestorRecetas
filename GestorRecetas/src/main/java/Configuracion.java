public class Configuracion {

    // Atributos que se guardan en configuracion.json
    private int ultimaOpcion;
    private int totalRecetas;

    // Constructor vacío necesario para Gson
    public Configuracion() {
    }

    // Constructor con parámetros
    public Configuracion(int ultimaOpcion, int totalRecetas) {
        this.ultimaOpcion = ultimaOpcion;
        this.totalRecetas = totalRecetas;
    }

    public int getUltimaOpcion() {
        return ultimaOpcion;
    }

    public int getTotalRecetas() {
        return totalRecetas;
    }

    public void setUltimaOpcion(int ultimaOpcion) {
        this.ultimaOpcion = ultimaOpcion;
    }

    public void setTotalRecetas(int totalRecetas) {
        this.totalRecetas = totalRecetas;
    }

    @Override
    public String toString() {
        return "Última opción: " + ultimaOpcion +
                " | Recetas almacenadas: " + totalRecetas;
    }
}
