import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Procesa los archivos planos generados por {@link GenerateInfoFiles} y
 * produce los dos reportes solicitados por el proyecto:
 * <ol>
 * <li>Un reporte de vendedores ordenado por dinero recaudado, de mayor a
 * menor.</li>
 * <li>Un reporte de productos ordenado por cantidad vendida, de mayor a
 * menor.</li>
 * </ol>
 *
 * <p>
 * Esta clase no solicita información al usuario: toma como entrada los
 * archivos <code>productos.txt</code>, <code>vendedores.txt</code> y todos
 * los archivos cuyo nombre comienza con <code>ventas_</code>, ubicados en el
 * directorio de trabajo del proyecto.
 * </p>
 *
 * <p>
 * Un mismo vendedor puede tener más de un archivo de ventas: la agrupación no
 * se hace por nombre de archivo sino por el tipo y número de documento que
 * cada archivo declara en su primera línea.
 * </p>
 *
 * @author Equipo de proyecto
 * @version 1.0
 */
public class main {

	/** Separador de campos usado en todos los archivos planos. */
	private static final String FIELD_SEPARATOR = ";";

	/** Codificación empleada para leer y escribir los archivos. */
	private static final String FILE_ENCODING = "UTF-8";

	/** Nombre del archivo de catálogo de productos. */
	private static final String PRODUCTS_FILE_NAME = "productos.txt";

	/** Nombre del archivo de información de vendedores. */
	private static final String SALESMEN_INFO_FILE_NAME = "vendedores.txt";

	/** Prefijo que identifica los archivos de ventas de cada vendedor. */
	private static final String SALES_FILE_PREFIX = "ventas_";

	/** Nombre del archivo de reporte de vendedores. */
	private static final String SALESMEN_REPORT_FILE_NAME = "reporte_vendedores.csv";

	/** Nombre del archivo de reporte de productos. */
	private static final String PRODUCTS_REPORT_FILE_NAME = "reporte_productos.csv";

	/**
	 * Punto de entrada del programa de generación de reportes.
	 *
	 * @param args argumentos de línea de comandos (no se utilizan).
	 */
	public static void main(String[] args) {
		try {
			Map<String, Product> products = loadProducts(PRODUCTS_FILE_NAME);
			Map<String, Salesman> salesmen = loadSalesmen(SALESMEN_INFO_FILE_NAME);

			processSalesFiles(new File("."), products, salesmen);

			writeSalesmenReport(salesmen);
			writeProductsReport(products);

			System.out.println("Generación de reportes finalizada con éxito.");
			System.out.println("Archivos creados: " + SALESMEN_REPORT_FILE_NAME + ", " + PRODUCTS_REPORT_FILE_NAME);
		} catch (IOException exception) {
			System.err.println("Error: no fue posible generar los reportes.");
			System.err.println("Detalle: " + exception.getMessage());
		} catch (IllegalStateException exception) {
			System.err.println("Error: los archivos de entrada no existen o no son válidos.");
			System.err.println("Detalle: " + exception.getMessage());
		}
	}

	/**
	 * Carga el catálogo de productos desde el archivo indicado.
	 *
	 * @param fileName nombre del archivo de productos.
	 * @return un mapa de productos indexado por su identificador.
	 * @throws IOException si el archivo no existe o no puede leerse.
	 */
	private static Map<String, Product> loadProducts(String fileName) throws IOException {
		Map<String, Product> products = new HashMap<String, Product>();

		BufferedReader reader = null;
		try {
			reader = openReader(fileName);
			String line;
			int lineNumber = 0;

			while ((line = reader.readLine()) != null) {
				lineNumber++;
				if (line.trim().isEmpty()) {
					continue;
				}

				String[] fields = line.split(FIELD_SEPARATOR);
				if (fields.length < 3) {
					warnMalformedLine(fileName, lineNumber, line);
					continue;
				}

				String productId = fields[0].trim();
				String productName = fields[1].trim();
				double unitPrice = parsePositiveDouble(fields[2].trim());

				if (unitPrice < 0) {
					warnMalformedLine(fileName, lineNumber, line);
					continue;
				}

				products.put(productId, new Product(productId, productName, unitPrice));
			}
		} finally {
			closeQuietly(reader);
		}

		return products;
	}

	/**
	 * Carga la información de los vendedores desde el archivo indicado.
	 *
	 * @param fileName nombre del archivo de vendedores.
	 * @return un mapa de vendedores indexado por la clave tipo+número de
	 *         documento.
	 * @throws IOException si el archivo no existe o no puede leerse.
	 */
	private static Map<String, Salesman> loadSalesmen(String fileName) throws IOException {
		Map<String, Salesman> salesmen = new HashMap<String, Salesman>();

		BufferedReader reader = null;
		try {
			reader = openReader(fileName);
			String line;
			int lineNumber = 0;

			while ((line = reader.readLine()) != null) {
				lineNumber++;
				if (line.trim().isEmpty()) {
					continue;
				}

				String[] fields = line.split(FIELD_SEPARATOR);
				if (fields.length < 4) {
					warnMalformedLine(fileName, lineNumber, line);
					continue;
				}

				String documentType = fields[0].trim();
				String documentNumber = fields[1].trim();
				String firstNames = fields[2].trim();
				String lastNames = fields[3].trim();

				String key = buildSalesmanKey(documentType, documentNumber);
				salesmen.put(key, new Salesman(documentType, documentNumber, firstNames, lastNames));
			}
		} finally {
			closeQuietly(reader);
		}

		return salesmen;
	}

	/**
	 * Busca y procesa todos los archivos de ventas del directorio dado,
	 * acumulando el dinero recaudado por cada vendedor y la cantidad vendida de
	 * cada producto.
	 *
	 * @param directory directorio donde se buscan los archivos de ventas.
	 * @param products  catálogo de productos ya cargado.
	 * @param salesmen  vendedores ya cargados.
	 * @throws IOException si ocurre un error al leer alguno de los archivos.
	 */
	private static void processSalesFiles(File directory, Map<String, Product> products, Map<String, Salesman> salesmen)
			throws IOException {

		File[] salesFiles = directory.listFiles();
		if (salesFiles == null) {
			throw new IllegalStateException("No fue posible listar el directorio de archivos de ventas.");
		}

		for (File file : salesFiles) {
			if (file.isFile() && file.getName().startsWith(SALES_FILE_PREFIX)) {
				processSingleSalesFile(file, products, salesmen);
			}
		}
	}

	/**
	 * Procesa un único archivo de ventas, sumando al vendedor correspondiente el
	 * dinero recaudado y a cada producto la cantidad vendida.
	 *
	 * <p>
	 * Las líneas mal formadas, los productos inexistentes y las cantidades
	 * negativas se ignoran y se informan por consola, sin detener el
	 * procesamiento del resto del archivo.
	 * </p>
	 *
	 * @param file     archivo de ventas a procesar.
	 * @param products catálogo de productos ya cargado.
	 * @param salesmen vendedores ya cargados.
	 * @throws IOException si ocurre un error al leer el archivo.
	 */
	private static void processSingleSalesFile(File file, Map<String, Product> products, Map<String, Salesman> salesmen)
			throws IOException {

		BufferedReader reader = null;
		try {
			reader = openReader(file.getPath());

			String headerLine = reader.readLine();
			if (headerLine == null || headerLine.trim().isEmpty()) {
				System.err.println("Advertencia: el archivo " + file.getName() + " está vacío. Se omite.");
				return;
			}

			String[] headerFields = headerLine.split(FIELD_SEPARATOR);
			if (headerFields.length < 2) {
				System.err.println(
						"Advertencia: el encabezado de " + file.getName() + " no tiene el formato esperado. Se omite.");
				return;
			}

			String documentType = headerFields[0].trim();
			String documentNumber = headerFields[1].trim();
			String key = buildSalesmanKey(documentType, documentNumber);

			Salesman salesman = salesmen.get(key);
			if (salesman == null) {
				System.err.println("Advertencia: el archivo " + file.getName()
						+ " pertenece a un vendedor que no está registrado en " + SALESMEN_INFO_FILE_NAME + ". Se omite.");
				return;
			}

			String line;
			int lineNumber = 1;

			while ((line = reader.readLine()) != null) {
				lineNumber++;
				if (line.trim().isEmpty()) {
					continue;
				}

				String[] fields = line.split(FIELD_SEPARATOR);
				if (fields.length < 2) {
					warnMalformedLine(file.getName(), lineNumber, line);
					continue;
				}

				String productId = fields[0].trim();
				int quantitySold = parsePositiveInt(fields[1].trim());

				if (quantitySold < 0) {
					warnMalformedLine(file.getName(), lineNumber, line);
					continue;
				}

				Product product = products.get(productId);
				if (product == null) {
					System.err.println("Advertencia: producto " + productId + " referenciado en " + file.getName()
							+ " (línea " + lineNumber + ") no existe en " + PRODUCTS_FILE_NAME + ". Se omite la línea.");
					continue;
				}

				salesman.addMoney(product.getUnitPrice() * quantitySold);
				product.addQuantitySold(quantitySold);
			}
		} finally {
			closeQuietly(reader);
		}
	}

	/**
	 * Escribe el reporte de vendedores ordenado por dinero recaudado, de mayor a
	 * menor.
	 *
	 * @param salesmen vendedores con sus totales ya acumulados.
	 * @throws IOException si ocurre un error al escribir el archivo.
	 */
	private static void writeSalesmenReport(Map<String, Salesman> salesmen) throws IOException {
		List<Salesman> orderedSalesmen = new ArrayList<Salesman>(salesmen.values());

		Collections.sort(orderedSalesmen, new Comparator<Salesman>() {
			@Override
			public int compare(Salesman first, Salesman second) {
				return Double.compare(second.getTotalMoney(), first.getTotalMoney());
			}
		});

		Writer writer = null;
		try {
			writer = openWriter(SALESMEN_REPORT_FILE_NAME);

			for (Salesman salesman : orderedSalesmen) {
				writer.write(salesman.getFullName() + FIELD_SEPARATOR + formatAmount(salesman.getTotalMoney()));
				writer.write(System.lineSeparator());
			}
		} finally {
			closeQuietly(writer);
		}
	}

	/**
	 * Escribe el reporte de productos ordenado por cantidad vendida, de mayor a
	 * menor.
	 *
	 * @param products productos con sus totales ya acumulados.
	 * @throws IOException si ocurre un error al escribir el archivo.
	 */
	private static void writeProductsReport(Map<String, Product> products) throws IOException {
		List<Product> orderedProducts = new ArrayList<Product>(products.values());

		Collections.sort(orderedProducts, new Comparator<Product>() {
			@Override
			public int compare(Product first, Product second) {
				return Integer.compare(second.getQuantitySold(), first.getQuantitySold());
			}
		});

		Writer writer = null;
		try {
			writer = openWriter(PRODUCTS_REPORT_FILE_NAME);

			for (Product product : orderedProducts) {
				writer.write(product.getName() + FIELD_SEPARATOR + formatAmount(product.getUnitPrice()));
				writer.write(System.lineSeparator());
			}
		} finally {
			closeQuietly(writer);
		}
	}

	/**
	 * Construye la clave usada para identificar a un vendedor a partir de su tipo
	 * y número de documento.
	 *
	 * @param documentType   tipo de documento.
	 * @param documentNumber número de documento.
	 * @return la clave del vendedor.
	 */
	private static String buildSalesmanKey(String documentType, String documentNumber) {
		return documentType + FIELD_SEPARATOR + documentNumber;
	}

	/**
	 * Interpreta un texto como número decimal no negativo.
	 *
	 * @param text texto a interpretar.
	 * @return el valor interpretado, o -1 si el texto no es un número válido o es
	 *         negativo.
	 */
	private static double parsePositiveDouble(String text) {
		try {
			double value = Double.parseDouble(text);
			return value < 0 ? -1 : value;
		} catch (NumberFormatException exception) {
			return -1;
		}
	}

	/**
	 * Interpreta un texto como número entero no negativo.
	 *
	 * @param text texto a interpretar.
	 * @return el valor interpretado, o -1 si el texto no es un número válido o es
	 *         negativo.
	 */
	private static int parsePositiveInt(String text) {
		try {
			int value = Integer.parseInt(text);
			return value < 0 ? -1 : value;
		} catch (NumberFormatException exception) {
			return -1;
		}
	}

	/**
	 * Da formato a un monto o precio para incluirlo en los reportes, sin
	 * decimales innecesarios.
	 *
	 * @param amount monto a formatear.
	 * @return el monto formateado como texto.
	 */
	private static String formatAmount(double amount) {
		if (amount == Math.floor(amount)) {
			return String.valueOf((long) amount);
		}
		return String.valueOf(amount);
	}

	/**
	 * Informa por consola que una línea de un archivo no tiene el formato
	 * esperado y fue omitida.
	 *
	 * @param fileName   nombre del archivo que contiene la línea.
	 * @param lineNumber número de la línea, contado desde 1.
	 * @param line       contenido de la línea.
	 */
	private static void warnMalformedLine(String fileName, int lineNumber, String line) {
		System.err.println(
				"Advertencia: línea mal formada en " + fileName + " (línea " + lineNumber + "): \"" + line + "\". Se omite.");
	}

	/**
	 * Abre un flujo de lectura de texto sobre el archivo indicado.
	 *
	 * @param fileName nombre o ruta del archivo a leer.
	 * @return el flujo de lectura listo para usarse.
	 * @throws IOException si el archivo no puede abrirse.
	 */
	private static BufferedReader openReader(String fileName) throws IOException {
		return new BufferedReader(new InputStreamReader(new FileInputStream(fileName), FILE_ENCODING));
	}

	/**
	 * Abre un flujo de escritura de texto sobre el archivo indicado.
	 *
	 * @param fileName nombre del archivo a crear o sobrescribir.
	 * @return el flujo de escritura listo para usarse.
	 * @throws IOException si el archivo no puede abrirse.
	 */
	private static Writer openWriter(String fileName) throws IOException {
		return new OutputStreamWriter(new FileOutputStream(fileName), FILE_ENCODING);
	}

	/**
	 * Cierra un flujo ignorando los errores de cierre.
	 *
	 * @param closeable flujo a cerrar; puede ser {@code null}.
	 */
	private static void closeQuietly(java.io.Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException exception) {
				System.err.println("Advertencia: no fue posible cerrar un archivo correctamente.");
			}
		}
	}

	/**
	 * Representa un producto del catálogo junto con la cantidad total vendida de
	 * él, acumulada durante el procesamiento de los archivos de ventas.
	 */
	private static class Product {

		/** Identificador del producto. */
		private final String id;

		/** Nombre del producto. */
		private final String name;

		/** Precio por unidad del producto. */
		private final double unitPrice;

		/** Cantidad total vendida del producto. */
		private int quantitySold;

		/**
		 * Crea un producto con la información indicada y cantidad vendida en cero.
		 *
		 * @param id        identificador del producto.
		 * @param name      nombre del producto.
		 * @param unitPrice precio por unidad.
		 */
		public Product(String id, String name, double unitPrice) {
			this.id = id;
			this.name = name;
			this.unitPrice = unitPrice;
			this.quantitySold = 0;
		}

		/**
		 * Suma unidades a la cantidad total vendida del producto.
		 *
		 * @param quantity cantidad a sumar.
		 */
		public void addQuantitySold(int quantity) {
			this.quantitySold += quantity;
		}

		/**
		 * @return el identificador del producto.
		 */
		public String getId() {
			return id;
		}

		/**
		 * @return el nombre del producto.
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return el precio por unidad del producto.
		 */
		public double getUnitPrice() {
			return unitPrice;
		}

		/**
		 * @return la cantidad total vendida del producto.
		 */
		public int getQuantitySold() {
			return quantitySold;
		}
	}

	/**
	 * Representa un vendedor junto con el dinero total que ha recaudado,
	 * acumulado durante el procesamiento de los archivos de ventas.
	 */
	private static class Salesman {

		/** Tipo de documento del vendedor. */
		private final String documentType;

		/** Número de documento del vendedor. */
		private final String documentNumber;

		/** Nombres del vendedor. */
		private final String firstNames;

		/** Apellidos del vendedor. */
		private final String lastNames;

		/** Dinero total recaudado por el vendedor. */
		private double totalMoney;

		/**
		 * Crea un vendedor con la información indicada y dinero recaudado en cero.
		 *
		 * @param documentType   tipo de documento.
		 * @param documentNumber número de documento.
		 * @param firstNames     nombres.
		 * @param lastNames      apellidos.
		 */
		public Salesman(String documentType, String documentNumber, String firstNames, String lastNames) {
			this.documentType = documentType;
			this.documentNumber = documentNumber;
			this.firstNames = firstNames;
			this.lastNames = lastNames;
			this.totalMoney = 0;
		}

		/**
		 * Suma dinero al total recaudado por el vendedor.
		 *
		 * @param money monto a sumar.
		 */
		public void addMoney(double money) {
			this.totalMoney += money;
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
		public String getDocumentNumber() {
			return documentNumber;
		}

		/**
		 * @return el nombre completo del vendedor.
		 */
		public String getFullName() {
			return firstNames + " " + lastNames;
		}

		/**
		 * @return el dinero total recaudado por el vendedor.
		 */
		public double getTotalMoney() {
			return totalMoney;
		}
	}
}
