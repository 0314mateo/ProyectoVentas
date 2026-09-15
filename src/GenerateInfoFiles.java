import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Genera los archivos planos pseudoaleatorios que sirven como entrada al
 * programa principal del proyecto (reportes de ventas).
 *
 * <p>
 * Los archivos producidos son:
 * </p>
 * <ul>
 * <li><b>productos.txt</b>: catálogo de productos con id, nombre y precio
 * unitario.</li>
 * <li><b>vendedores.txt</b>: información de los vendedores (tipo y número de
 * documento, nombres y apellidos).</li>
 * <li><b>ventas_TIPODOC_NUMERODOC[_n].txt</b>: un archivo (o varios) de ventas
 * por cada vendedor.</li>
 * </ul>
 *
 * <p>
 * Esta clase no solicita información al usuario: todos los parámetros de
 * generación están definidos como constantes.
 * </p>
 *
 * @author Equipo de proyecto
 * @version 1.0
 */
public class GenerateInfoFiles {

	/** Separador de campos usado en todos los archivos planos. */
	private static final String FIELD_SEPARATOR = ";";

	/** Codificación empleada para escribir los archivos. */
	private static final String FILE_ENCODING = "UTF-8";

	/** Nombre del archivo de catálogo de productos. */
	private static final String PRODUCTS_FILE_NAME = "productos.txt";

	/** Nombre del archivo de información de vendedores. */
	private static final String SALESMEN_INFO_FILE_NAME = "vendedores.txt";

	/** Prefijo de los archivos de ventas de cada vendedor. */
	private static final String SALES_FILE_PREFIX = "ventas_";

	/** Extensión de los archivos generados. */
	private static final String FILE_EXTENSION = ".txt";

	/** Cantidad de productos del catálogo a generar. */
	private static final int PRODUCTS_COUNT = 15;

	/** Cantidad de vendedores a generar. */
	private static final int SALESMEN_COUNT = 8;

	/** Cantidad mínima de líneas de venta por archivo de vendedor. */
	private static final int MIN_SALES_PER_FILE = 3;

	/** Cantidad máxima de líneas de venta por archivo de vendedor. */
	private static final int MAX_SALES_PER_FILE = 10;

	/** Cantidad máxima de archivos de ventas que puede tener un vendedor. */
	private static final int MAX_FILES_PER_SALESMAN = 2;

	/** Cantidad mínima de unidades vendidas en una línea de venta. */
	private static final int MIN_QUANTITY_SOLD = 1;

	/** Cantidad máxima de unidades vendidas en una línea de venta. */
	private static final int MAX_QUANTITY_SOLD = 20;

	/** Precio unitario mínimo de un producto, en pesos. */
	private static final int MIN_PRODUCT_PRICE = 1000;

	/** Precio unitario máximo de un producto, en pesos. */
	private static final int MAX_PRODUCT_PRICE = 500000;

	/** Tipos de documento válidos para los vendedores. */
	private static final String[] DOCUMENT_TYPES = { "CC", "CE", "TI", "PA" };

	/** Nombres reales usados para construir vendedores coherentes. */
	private static final String[] FIRST_NAMES = { "Andrés", "Camila", "Daniela", "Felipe", "Gabriela", "Hernán",
			"Isabella", "Juan Pablo", "Laura", "Mateo", "Natalia", "Óscar", "Paula", "Ricardo", "Santiago", "Valentina" };

	/** Apellidos reales usados para construir vendedores coherentes. */
	private static final String[] LAST_NAMES = { "Álvarez", "Betancur", "Cardona", "Duarte", "Escobar", "Fernández",
			"Gómez", "Herrera", "Jaramillo", "López", "Martínez", "Ospina", "Peláez", "Quintero", "Restrepo", "Sánchez" };

	/** Sustantivos base para armar nombres de productos coherentes. */
	private static final String[] PRODUCT_NOUNS = { "Teclado", "Monitor", "Mouse", "Audífonos", "Impresora", "Portátil",
			"Tablet", "Cámara", "Parlante", "Disco Duro", "Memoria USB", "Router", "Silla Ergonómica", "Escritorio",
			"Cargador", "Micrófono", "Webcam", "Estabilizador" };

	/** Adjetivos o líneas comerciales para diferenciar los productos. */
	private static final String[] PRODUCT_LINES = { "Básico", "Pro", "Plus", "Compacto", "Premium", "Gamer",
			"Empresarial" };

	/** Generador pseudoaleatorio compartido por todos los métodos. */
	private static final Random RANDOM = new Random();

	/** Identificadores de los productos efectivamente generados. */
	private static final List<String> generatedProductIds = new ArrayList<String>();

	/** Vendedores efectivamente generados. */
	private static final List<Salesman> generatedSalesmen = new ArrayList<Salesman>();

	/**
	 * Punto de entrada del generador de archivos de prueba.
	 *
	 * <p>
	 * Crea el catálogo de productos, el archivo de vendedores y, para cada
	 * vendedor, uno o más archivos de ventas. Informa por consola el resultado de
	 * la ejecución.
	 * </p>
	 *
	 * @param args argumentos de línea de comandos (no se utilizan).
	 */
	public static void main(String[] args) {
		try {
			createProductsFile(PRODUCTS_COUNT);
			createSalesManInfoFile(SALESMEN_COUNT);
			createAllSalesFiles();

			System.out.println("Generación de archivos finalizada con éxito.");
			System.out.println("Ubicación: " + new File("").getAbsolutePath());
		} catch (IOException exception) {
			System.err.println("Error: no fue posible generar los archivos de prueba.");
			System.err.println("Detalle: " + exception.getMessage());
		} catch (IllegalArgumentException exception) {
			System.err.println("Error: parámetros de generación inválidos.");
			System.err.println("Detalle: " + exception.getMessage());
		}
	}

	/**
	 * Crea un archivo pseudoaleatorio con la información de ventas de un vendedor.
	 *
	 * <p>
	 * La primera línea contiene el tipo y el número de documento del vendedor; las
	 * siguientes contienen el identificador del producto y la cantidad vendida.
	 * </p>
	 *
	 * @param randomSalesCount cantidad de líneas de venta a generar; debe ser mayor
	 *                         que cero.
	 * @param name             nombre con el que se identifica el vendedor.
	 * @param id               número de documento del vendedor.
	 * @throws IOException              si ocurre un error al escribir el archivo.
	 * @throws IllegalArgumentException si los parámetros recibidos no son válidos.
	 */
	public static void createSalesMenFile(int randomSalesCount, String name, long id) throws IOException {
		createSalesMenFile(randomSalesCount, name, id, DOCUMENT_TYPES[0], 1);
	}

	/**
	 * Crea un archivo pseudoaleatorio de ventas de un vendedor permitiendo indicar
	 * el tipo de documento y el consecutivo del archivo.
	 *
	 * <p>
	 * El consecutivo habilita que un mismo vendedor tenga más de un archivo de
	 * ventas, tal como lo contempla el punto adicional del enunciado.
	 * </p>
	 *
	 * @param randomSalesCount cantidad de líneas de venta a generar; debe ser mayor
	 *                         que cero.
	 * @param name             nombre con el que se identifica el vendedor.
	 * @param id               número de documento del vendedor.
	 * @param documentType     tipo de documento del vendedor.
	 * @param fileNumber       consecutivo del archivo para ese vendedor.
	 * @throws IOException              si ocurre un error al escribir el archivo.
	 * @throws IllegalArgumentException si los parámetros recibidos no son válidos.
	 */
	public static void createSalesMenFile(int randomSalesCount, String name, long id, String documentType,
			int fileNumber) throws IOException {

		if (randomSalesCount <= 0) {
			throw new IllegalArgumentException("La cantidad de ventas debe ser mayor que cero.");
		}
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("El nombre del vendedor no puede estar vacío.");
		}
		if (id <= 0) {
			throw new IllegalArgumentException("El número de documento debe ser positivo.");
		}
		if (generatedProductIds.isEmpty()) {
			throw new IllegalArgumentException(
					"Debe generarse primero el archivo de productos con createProductsFile(int).");
		}

		String fileName = buildSalesFileName(documentType, id, fileNumber);
		Writer writer = null;
		try {
			writer = openWriter(fileName);
			writer.write(documentType + FIELD_SEPARATOR + id);
			writer.write(System.lineSeparator());

			for (int saleIndex = 0; saleIndex < randomSalesCount; saleIndex++) {
				String productId = generatedProductIds.get(RANDOM.nextInt(generatedProductIds.size()));
				int quantity = randomBetween(MIN_QUANTITY_SOLD, MAX_QUANTITY_SOLD);

				writer.write(productId + FIELD_SEPARATOR + quantity + FIELD_SEPARATOR);
				writer.write(System.lineSeparator());
			}
		} finally {
			closeQuietly(writer);
		}
	}

	/**
	 * Crea el archivo con la información pseudoaleatoria del catálogo de productos.
	 *
	 * <p>
	 * Cada línea tiene el formato
	 * <code>IDProducto;NombreProducto;PrecioPorUnidad</code>. Los identificadores
	 * generados quedan disponibles para los archivos de ventas.
	 * </p>
	 *
	 * @param productsCount cantidad de productos a generar; debe ser mayor que
	 *                      cero.
	 * @throws IOException              si ocurre un error al escribir el archivo.
	 * @throws IllegalArgumentException si la cantidad recibida no es válida.
	 */
	public static void createProductsFile(int productsCount) throws IOException {
		if (productsCount <= 0) {
			throw new IllegalArgumentException("La cantidad de productos debe ser mayor que cero.");
		}

		generatedProductIds.clear();

		Writer writer = null;
		try {
			writer = openWriter(PRODUCTS_FILE_NAME);

			for (int productIndex = 1; productIndex <= productsCount; productIndex++) {
				String productId = String.format("P%03d", productIndex);
				String productName = buildProductName();
				int unitPrice = randomBetween(MIN_PRODUCT_PRICE / 100, MAX_PRODUCT_PRICE / 100) * 100;

				writer.write(productId + FIELD_SEPARATOR + productName + FIELD_SEPARATOR + unitPrice);
				writer.write(System.lineSeparator());

				generatedProductIds.add(productId);
			}
		} finally {
			closeQuietly(writer);
		}
	}

	/**
	 * Crea el archivo con la información pseudoaleatoria de los vendedores.
	 *
	 * <p>
	 * Cada línea tiene el formato
	 * <code>TipoDocumento;NúmeroDocumento;Nombres;Apellidos</code>. Los nombres y
	 * apellidos se toman de listas de nombres reales y los números de documento no
	 * se repiten.
	 * </p>
	 *
	 * @param salesmanCount cantidad de vendedores a generar; debe ser mayor que
	 *                      cero.
	 * @throws IOException              si ocurre un error al escribir el archivo.
	 * @throws IllegalArgumentException si la cantidad recibida no es válida.
	 */
	public static void createSalesManInfoFile(int salesmanCount) throws IOException {
		if (salesmanCount <= 0) {
			throw new IllegalArgumentException("La cantidad de vendedores debe ser mayor que cero.");
		}

		generatedSalesmen.clear();

		Writer writer = null;
		try {
			writer = openWriter(SALESMEN_INFO_FILE_NAME);

			for (int salesmanIndex = 0; salesmanIndex < salesmanCount; salesmanIndex++) {
				String documentType = DOCUMENT_TYPES[RANDOM.nextInt(DOCUMENT_TYPES.length)];
				long documentNumber = buildUniqueDocumentNumber();
				String firstNames = FIRST_NAMES[RANDOM.nextInt(FIRST_NAMES.length)];
				String lastNames = LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)] + " "
						+ LAST_NAMES[RANDOM.nextInt(LAST_NAMES.length)];

				writer.write(documentType + FIELD_SEPARATOR + documentNumber + FIELD_SEPARATOR + firstNames
						+ FIELD_SEPARATOR + lastNames);
				writer.write(System.lineSeparator());

				generatedSalesmen.add(new Salesman(documentType, documentNumber, firstNames, lastNames));
			}
		} finally {
			closeQuietly(writer);
		}
	}

	/**
	 * Crea los archivos de ventas de todos los vendedores generados previamente.
	 *
	 * <p>
	 * A cada vendedor se le asigna una cantidad pseudoaleatoria de archivos, con el
	 * fin de ejercitar el procesamiento de más de un archivo por vendedor.
	 * </p>
	 *
	 * @throws IOException si ocurre un error al escribir alguno de los archivos.
	 */
	private static void createAllSalesFiles() throws IOException {
		for (Salesman salesman : generatedSalesmen) {
			int filesForSalesman = randomBetween(1, MAX_FILES_PER_SALESMAN);

			for (int fileNumber = 1; fileNumber <= filesForSalesman; fileNumber++) {
				int salesCount = randomBetween(MIN_SALES_PER_FILE, MAX_SALES_PER_FILE);

				createSalesMenFile(salesCount, salesman.getFullName(), salesman.getDocumentNumber(),
						salesman.getDocumentType(), fileNumber);
			}
		}
	}

	/**
	 * Construye el nombre del archivo de ventas de un vendedor.
	 *
	 * @param documentType   tipo de documento del vendedor.
	 * @param documentNumber número de documento del vendedor.
	 * @param fileNumber     consecutivo del archivo.
	 * @return el nombre del archivo de ventas.
	 */
	private static String buildSalesFileName(String documentType, long documentNumber, int fileNumber) {
		String baseName = SALES_FILE_PREFIX + documentType + "_" + documentNumber;

		if (fileNumber > 1) {
			baseName = baseName + "_" + fileNumber;
		}
		return baseName + FILE_EXTENSION;
	}

	/**
	 * Construye un nombre de producto combinando un sustantivo y una línea
	 * comercial.
	 *
	 * @return el nombre del producto.
	 */
	private static String buildProductName() {
		return PRODUCT_NOUNS[RANDOM.nextInt(PRODUCT_NOUNS.length)] + " "
				+ PRODUCT_LINES[RANDOM.nextInt(PRODUCT_LINES.length)];
	}

	/**
	 * Genera un número de documento que no haya sido asignado a otro vendedor.
	 *
	 * @return un número de documento único dentro de la ejecución actual.
	 */
	private static long buildUniqueDocumentNumber() {
		long documentNumber;

		do {
			documentNumber = 1000000000L + (long) (RANDOM.nextDouble() * 99999999L);
		} while (isDocumentNumberUsed(documentNumber));

		return documentNumber;
	}

	/**
	 * Indica si un número de documento ya fue asignado a un vendedor generado.
	 *
	 * @param documentNumber número de documento a verificar.
	 * @return {@code true} si el número ya está en uso; {@code false} en caso
	 *         contrario.
	 */
	private static boolean isDocumentNumberUsed(long documentNumber) {
		for (Salesman salesman : generatedSalesmen) {
			if (salesman.getDocumentNumber() == documentNumber) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Retorna un entero pseudoaleatorio dentro de un rango cerrado.
	 *
	 * @param minimumValue valor mínimo incluido.
	 * @param maximumValue valor máximo incluido.
	 * @return un entero entre {@code minimumValue} y {@code maximumValue}.
	 */
	private static int randomBetween(int minimumValue, int maximumValue) {
		return minimumValue + RANDOM.nextInt((maximumValue - minimumValue) + 1);
	}

	/**
	 * Abre un flujo de escritura de texto sobre el archivo indicado.
	 *
	 * @param fileName nombre del archivo a crear o sobrescribir.
	 * @return el flujo de escritura listo para usarse.
	 * @throws IOException si el archivo no puede abrirse.
	 */
	private static Writer openWriter(String fileName) throws IOException {
		return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), FILE_ENCODING));
	}

	/**
	 * Cierra un flujo de escritura ignorando los errores de cierre.
	 *
	 * @param writer flujo a cerrar; puede ser {@code null}.
	 */
	private static void closeQuietly(Writer writer) {
		if (writer != null) {
			try {
				writer.close();
			} catch (IOException exception) {
				System.err.println("Advertencia: no fue posible cerrar el archivo correctamente.");
			}
		}
	}

	/**
	 * Representa los datos básicos de un vendedor generado.
	 */
	private static class Salesman {

		/** Tipo de documento del vendedor. */
		private final String documentType;

		/** Número de documento del vendedor. */
		private final long documentNumber;

		/** Nombres del vendedor. */
		private final String firstNames;

		/** Apellidos del vendedor. */
		private final String lastNames;

		/**
		 * Crea un vendedor con la información indicada.
		 *
		 * @param documentType   tipo de documento.
		 * @param documentNumber número de documento.
		 * @param firstNames     nombres.
		 * @param lastNames      apellidos.
		 */
		public Salesman(String documentType, long documentNumber, String firstNames, String lastNames) {
			this.documentType = documentType;
			this.documentNumber = documentNumber;
			this.firstNames = firstNames;
			this.lastNames = lastNames;
		}

		/**
		 * @return el tipo de documento del vendedor.
		 */
		public String getDocumentType() {
			return documentType;
		}

		/**
		 * @return el número de documento del vendedor.
		 */
		public long getDocumentNumber() {
			return documentNumber;
		}

		/**
		 * @return el nombre completo del vendedor.
		 */
		public String getFullName() {
			return firstNames + " " + lastNames;
		}
	}
}
