import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class GestionRecetas {

    // Directorio donde se almacenan los documentos (ruta relativa al proyecto)
    private static final String RUTA_FICHERO = "src/main/resources/";
    // Nombres de los documentos utilizados por la aplicación
    private static final String FICHERO_BINARIO = "recetas.dat";
    private static final String FICHERO_XML = "recetas.xml";
    private static final String FICHERO_JSON = "configuracion.json";

    // Lectura de datos por teclado
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        mostrarCabecera();

        // Recupera la configuración guardada en la ejecución anterior
        Configuracion configuracion = cargarConfiguracion();
        System.out.println("Configuración recuperada -> " + configuracion);

        // Comprueba que existe recetas.dat y, si no, crea el catálogo inicial
        comprobarCatalogo();

        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opción: ");

            switch (opcion) {
                case 1:
                    crearCatalogo();
                    break;
                case 2:
                    mostrarRecetas();
                    break;
                case 3:
                    buscarReceta();
                    break;
                case 4:
                    modificarTiempo();
                    break;
                case 5:
                    anadirReceta();
                    break;
                case 6:
                    exportarXML();
                    break;
                case 0:
                    System.out.println("Fin del programa.");
                    break;
                default:
                    System.out.println("Opción no válida.");
            }

            // Se guarda la última opción del menú que realiza una operación
            if (opcion >= 1 && opcion <= 6) {
                configuracion.setUltimaOpcion(opcion);
            }
        } while (opcion != 0);

        guardarConfiguracion(configuracion);
        sc.close();
    }

    // ------------------------------------------------------------------
    // Menú y lectura de datos
    // ------------------------------------------------------------------

    // Muestra el usuario, la fecha y la hora de ejecución
    private static void mostrarCabecera() {
        String fecha = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        System.out.println("*** GESTOR DE RECETAS ***");
        System.out.println("Usuario: " + System.getProperty("user.name")
                + " | Fecha y hora: " + fecha);
    }

    private static void mostrarMenu() {
        System.out.println("\n*** MENÚ ***");
        System.out.println("1. Crear catálogo inicial");
        System.out.println("2. Mostrar recetas");
        System.out.println("3. Buscar receta por identificador");
        System.out.println("4. Modificar tiempo de preparación");
        System.out.println("5. Añadir receta");
        System.out.println("6. Exportar catálogo a XML");
        System.out.println("0. Salir");
    }

    // Lee un número entero. Devuelve -1 si el texto no es un número
    private static int leerEntero(String mensaje) {
        System.out.print(mensaje);
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Error: debe introducir un número entero.");
            return -1;
        }
    }

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return sc.nextLine().trim();
    }

    // ------------------------------------------------------------------
    // Fichero binario recetas.dat
    // ------------------------------------------------------------------

    // Comprueba si recetas.dat existe antes de acceder a él
    private static void comprobarCatalogo() {
        // Si el directorio de documentos no existe, se crea
        File directorio = new File(RUTA_FICHERO);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        File fichero = new File(RUTA_FICHERO + FICHERO_BINARIO);
        if (fichero.exists()) {
            System.out.println("El fichero " + FICHERO_BINARIO
                    + " existe. Se utilizará el catálogo almacenado.");
        } else {
            System.out.println("El fichero " + FICHERO_BINARIO
                    + " no existe. Se crea el catálogo inicial.");
            crearCatalogoInicial();
        }
    }

    // Opción 1 del menú: pide confirmación porque sobrescribe el catálogo
    private static void crearCatalogo() {
        String respuesta = leerTexto("Se sobrescribirá el catálogo actual. ¿Continuar? (s/n): ");
        if (respuesta.equalsIgnoreCase("s")) {
            crearCatalogoInicial();
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    // Crea el catálogo inicial con varias recetas
    private static void crearCatalogoInicial() {
        ArrayList<Receta> recetas = new ArrayList<>();
        recetas.add(new Receta(1, "Tortilla de patatas", "Plato principal", 45, "Media",
                new ArrayList<>(Arrays.asList("patatas", "huevos", "cebolla", "aceite de oliva", "sal"))));
        recetas.add(new Receta(2, "Gazpacho andaluz", "Entrante", 20, "Fácil",
                new ArrayList<>(Arrays.asList("tomate", "pepino", "pimiento", "ajo", "aceite de oliva", "vinagre"))));
        recetas.add(new Receta(3, "Paella valenciana", "Plato principal", 90, "Difícil",
                new ArrayList<>(Arrays.asList("arroz", "pollo", "judías verdes", "garrofón", "azafrán", "tomate"))));
        recetas.add(new Receta(4, "Croquetas de jamón", "Entrante", 60, "Media",
                new ArrayList<>(Arrays.asList("jamón", "harina", "leche", "mantequilla", "pan rallado", "huevo"))));
        recetas.add(new Receta(5, "Flan de huevo", "Postre", 50, "Media",
                new ArrayList<>(Arrays.asList("huevos", "leche", "azúcar", "vainilla"))));
        recetas.add(new Receta(6, "Ensalada mixta", "Entrante", 10, "Fácil",
                new ArrayList<>(Arrays.asList("lechuga", "tomate", "cebolla", "atún", "aceitunas"))));

        if (guardarRecetas(recetas)) {
            System.out.println("Catálogo inicial creado con " + recetas.size() + " recetas.");
        }
    }

    // Escribe todas las recetas en el fichero binario. Devuelve true si no hay error
    private static boolean guardarRecetas(ArrayList<Receta> recetas) {
        try (ObjectOutputStream escribirFichero = new ObjectOutputStream(
                new FileOutputStream(RUTA_FICHERO + FICHERO_BINARIO))) {

            for (Receta receta : recetas) {
                escribirFichero.writeObject(receta);
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error al escribir el fichero " + FICHERO_BINARIO
                    + ": " + e.getMessage());
            return false;
        }
    }

    // Lee todas las recetas del fichero binario. Devuelve null si hay error
    private static ArrayList<Receta> leerRecetas() {
        ArrayList<Receta> recetas = new ArrayList<>();

        try (ObjectInputStream leerFichero = new ObjectInputStream(
                new FileInputStream(RUTA_FICHERO + FICHERO_BINARIO))) {

            // Se leen objetos hasta que se alcanza el final del fichero
            while (true) {
                Receta receta = (Receta) leerFichero.readObject();
                recetas.add(receta);
            }
        } catch (EOFException e) {
            // Final normal del fichero: se han leído todas las recetas
            return recetas;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error al leer el fichero " + FICHERO_BINARIO
                    + ": " + e.getMessage());
            return null;
        }
    }

    // Busca una receta por su identificador. Devuelve null si no existe
    private static Receta buscarPorId(ArrayList<Receta> recetas, int id) {
        for (Receta receta : recetas) {
            if (receta.getId() == id) {
                return receta;
            }
        }
        return null;
    }

    // Opción 2: muestra todas las recetas
    private static void mostrarRecetas() {
        ArrayList<Receta> recetas = leerRecetas();
        if (recetas == null) {
            return;
        }
        System.out.println("--- Recetas almacenadas (" + recetas.size() + ") ---");
        for (Receta receta : recetas) {
            System.out.println(receta);
        }
    }

    // Opción 3: busca una receta por su identificador
    private static void buscarReceta() {
        int id = leerEntero("Identificador de la receta: ");
        if (id < 0) {
            return;
        }
        ArrayList<Receta> recetas = leerRecetas();
        if (recetas == null) {
            return;
        }
        Receta receta = buscarPorId(recetas, id);
        if (receta != null) {
            System.out.println(receta);
        } else {
            System.out.println("No existe ninguna receta con el identificador " + id + ".");
        }
    }

    // Opción 4: modifica el tiempo de preparación de una receta
    private static void modificarTiempo() {
        int id = leerEntero("Identificador de la receta: ");
        if (id < 0) {
            return;
        }
        int tiempo = leerEntero("Nuevo tiempo de preparación (minutos): ");
        if (tiempo <= 0) {
            System.out.println("El tiempo debe ser un número positivo. No se realiza ningún cambio.");
            return;
        }

        ArrayList<Receta> recetas = leerRecetas();
        if (recetas == null) {
            return;
        }
        Receta receta = buscarPorId(recetas, id);
        if (receta == null) {
            System.out.println("No existe ninguna receta con el identificador " + id + ".");
            return;
        }

        receta.setTiempoPreparacion(tiempo);
        if (guardarRecetas(recetas)) {
            System.out.println("Tiempo de preparación actualizado correctamente.");
        }
    }

    // Opción 5: añade una receta nueva al catálogo
    private static void anadirReceta() {
        String nombre = leerTexto("Nombre: ");
        String categoria = leerTexto("Categoría: ");
        int tiempo = leerEntero("Tiempo de preparación (minutos): ");
        String dificultad = leerTexto("Dificultad (Fácil/Media/Difícil): ");
        String texto = leerTexto("Ingredientes (separados por comas): ");

        // Se valida la información antes de almacenarla
        if (nombre.isEmpty() || tiempo <= 0) {
            System.out.println("Datos no válidos: el nombre no puede estar vacío y el tiempo debe ser positivo.");
            return;
        }

        ArrayList<String> ingredientes = new ArrayList<>();
        for (String ingrediente : texto.split(",")) {
            if (!ingrediente.trim().isEmpty()) {
                ingredientes.add(ingrediente.trim());
            }
        }

        ArrayList<Receta> recetas = leerRecetas();
        if (recetas == null) {
            return;
        }

        // El nuevo identificador es el mayor existente más uno
        int nuevoId = 1;
        for (Receta receta : recetas) {
            nuevoId = Math.max(nuevoId, receta.getId() + 1);
        }

        recetas.add(new Receta(nuevoId, nombre, categoria, tiempo, dificultad, ingredientes));
        if (guardarRecetas(recetas)) {
            System.out.println("Receta añadida con el identificador " + nuevoId + ".");
        }
    }

    // ------------------------------------------------------------------
    // Documento XML recetas.xml
    // ------------------------------------------------------------------

    // Opción 6: exporta el catálogo a un documento XML mediante DOM
    private static void exportarXML() {
        ArrayList<Receta> recetas = leerRecetas();
        if (recetas == null) {
            return;
        }

        try {
            // Crea el analizador DOM y un documento XML vacío
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document documento = builder.newDocument();

            // Crea el elemento raíz
            Element raiz = documento.createElement("recetas");
            raiz.setAttribute("total", String.valueOf(recetas.size()));
            documento.appendChild(raiz);

            // Añade un elemento <receta> por cada receta del catálogo
            for (Receta receta : recetas) {
                raiz.appendChild(crearElementoReceta(documento, receta));
            }

            // Guarda el documento XML con sangrado para que sea legible
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(documento),
                    new StreamResult(new File(RUTA_FICHERO + FICHERO_XML)));

            System.out.println("Catálogo exportado correctamente a " + FICHERO_XML + ".");
        } catch (Exception e) {
            System.out.println("Error al crear el documento XML: " + e.getMessage());
        }
    }

    // Crea el elemento <receta> con todos sus datos
    private static Element crearElementoReceta(Document documento, Receta receta) {
        Element elemento = documento.createElement("receta");
        elemento.setAttribute("id", String.valueOf(receta.getId()));

        elemento.appendChild(crearElemento(documento, "nombre", receta.getNombre()));
        elemento.appendChild(crearElemento(documento, "categoria", receta.getCategoria()));
        elemento.appendChild(crearElemento(documento, "tiempoPreparacion",
                String.valueOf(receta.getTiempoPreparacion())));
        elemento.appendChild(crearElemento(documento, "dificultad", receta.getDificultad()));

        Element ingredientes = documento.createElement("ingredientes");
        for (String ingrediente : receta.getIngredientes()) {
            ingredientes.appendChild(crearElemento(documento, "ingrediente", ingrediente));
        }
        elemento.appendChild(ingredientes);

        return elemento;
    }

    // Crea un elemento con texto: <nombre>texto</nombre>
    private static Element crearElemento(Document documento, String nombre, String texto) {
        Element elemento = documento.createElement(nombre);
        elemento.setTextContent(texto);
        return elemento;
    }

    // ------------------------------------------------------------------
    // Documento JSON configuracion.json
    // ------------------------------------------------------------------

    // Recupera la configuración. Si no existe o es incorrecta, usa valores por defecto
    private static Configuracion cargarConfiguracion() {
        File fichero = new File(RUTA_FICHERO + FICHERO_JSON);
        if (!fichero.exists()) {
            System.out.println("No existe " + FICHERO_JSON
                    + " (primera ejecución). Se utiliza la configuración por defecto.");
            return new Configuracion();
        }

        Gson gson = new Gson();
        try (FileReader lector = new FileReader(RUTA_FICHERO + FICHERO_JSON)) {
            // Convierte el documento JSON en un objeto Configuracion
            Configuracion configuracion = gson.fromJson(lector, Configuracion.class);
            if (configuracion != null) {
                return configuracion;
            }
            System.out.println("El documento " + FICHERO_JSON + " está vacío. Se utiliza la configuración por defecto.");
        } catch (IOException | JsonParseException e) {
            System.out.println("Error al leer el documento " + FICHERO_JSON
                    + ". Se utiliza la configuración por defecto.");
        }
        return new Configuracion();
    }

    // Guarda la última opción utilizada y el número total de recetas
    private static void guardarConfiguracion(Configuracion configuracion) {
        // Si el catálogo no se puede leer, se conserva el total anterior
        ArrayList<Receta> recetas = leerRecetas();
        if (recetas != null) {
            configuracion.setTotalRecetas(recetas.size());
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter escritor = new FileWriter(RUTA_FICHERO + FICHERO_JSON)) {
            // Convierte el objeto Java en un documento JSON
            gson.toJson(configuracion, escritor);
            System.out.println("Configuración guardada en " + FICHERO_JSON + ".");
        } catch (IOException e) {
            System.out.println("Error al escribir el documento " + FICHERO_JSON + ".");
        }
    }
}
