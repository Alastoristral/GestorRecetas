import java.io.Serializable;
import java.util.ArrayList;

public class Receta implements Serializable {

    // Atributos de la receta
    private int id;
    private String nombre;
    private String categoria;
    private int tiempoPreparacion; // en minutos
    private String dificultad;
    private ArrayList<String> ingredientes;

    // Constructor con parámetros
    public Receta(int id, String nombre, String categoria, int tiempoPreparacion,
                  String dificultad, ArrayList<String> ingredientes) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.tiempoPreparacion = tiempoPreparacion;
        this.dificultad = dificultad;
        this.ingredientes = ingredientes;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getTiempoPreparacion() {
        return tiempoPreparacion;
    }

    public String getDificultad() {
        return dificultad;
    }

    public ArrayList<String> getIngredientes() {
        return ingredientes;
    }

    public void setTiempoPreparacion(int tiempoPreparacion) {
        this.tiempoPreparacion = tiempoPreparacion;
    }

    // Información de la receta
    @Override
    public String toString() {
        return "[" + id + "] " + nombre +
                " | Categoría: " + categoria +
                " | Tiempo: " + tiempoPreparacion + " min" +
                " | Dificultad: " + dificultad +
                "\n     Ingredientes: " + String.join(", ", ingredientes);
    }
}
