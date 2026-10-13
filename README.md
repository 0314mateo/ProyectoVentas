# Proyecto Ventas — Primera entrega

Generador de archivos planos pseudoaleatorios que servirán como entrada al programa
principal (segunda entrega).

## Contenido

- `src/GenerateInfoFiles.java` — única clase de esta entrega, con método `main`.
- `.project`, `.classpath`, `.settings/` — metadatos del proyecto de Eclipse (Java 8).

## Cómo ejecutar en Eclipse

1. `File > Import… > General > Existing Projects into Workspace`.
2. Seleccionar la carpeta `ProyectoVentas` y finalizar.
3. Clic derecho sobre `GenerateInfoFiles.java` → `Run As > Java Application`.

Los archivos se generan en la carpeta raíz del proyecto (el directorio de trabajo por
defecto de Eclipse), de modo que queden junto al programa, como pide el enunciado.

## Archivos que se generan

| Archivo | Formato de cada línea |
|---|---|
| `productos.txt` | `IDProducto;NombreProducto;PrecioPorUnidad` |
| `vendedores.txt` | `TipoDocumento;NúmeroDocumento;Nombres;Apellidos` |
| `ventas_TIPO_NUMERO[_n].txt` | 1.ª línea: `TipoDocumento;NúmeroDocumento` · resto: `IDProducto;Cantidad;` |

## Métodos exigidos por el enunciado

- `createSalesMenFile(int randomSalesCount, String name, long id)`
- `createProductsFile(int productsCount)`
- `createSalesManInfoFile(int salesmanCount)`

## Parámetros de generación

Se controlan mediante constantes al inicio de la clase (`PRODUCTS_COUNT`,
`SALESMEN_COUNT`, rangos de cantidades y precios, etc.). El programa **no solicita
información al usuario**.

## Consideraciones

- Los IDs de producto usados en las ventas siempre existen en `productos.txt`.
- Los números de documento de los vendedores no se repiten.
- Un mismo vendedor puede tener más de un archivo de ventas (punto extra *a*).
- Cantidades y precios siempre son positivos.
- Los archivos se escriben en UTF-8 para conservar las tildes.
- Se informa por consola el éxito o el error de la ejecución.


# Proyecto Ventas — Segunda entrega

Versión preliminar del proyecto completo: incluye las dos clases con método `main`
que pide el enunciado.

## Contenido

- `src/GenerateInfoFiles.java` — genera `productos.txt`, `vendedores.txt` y los
  archivos `ventas_*.txt`.
- `src/main.java` — procesa esos archivos y genera `reporte_vendedores.csv` y
  `reporte_productos.csv`.
- `.project`, `.classpath`, `.settings/` — metadatos del proyecto de Eclipse (Java 8).
- `EstadoDelProyecto.docx` — documento con las partes que aún le faltan al proyecto.

## Cómo ejecutar en Eclipse

1. `File > Import… > General > Existing Projects into Workspace`, seleccionar la
   carpeta `ProyectoVentas`.
2. Clic derecho sobre `GenerateInfoFiles.java` → `Run As > Java Application`
   (genera los archivos de entrada).
3. Refrescar el proyecto (F5).
4. Clic derecho sobre `main.java` → `Run As > Java Application` (genera los reportes).

Ningún programa solicita información al usuario, como exige el enunciado.

# Proyecto Ventas — Entrega final

Programa en Java 8 (Eclipse) que genera archivos planos de prueba y los procesa para
producir reportes de ventas.

## Clases (solo dos con método `main`)

- `src/GenerateInfoFiles.java` — genera los archivos de entrada pseudoaleatorios:
  `productos.txt`, `vendedores.txt` y los `ventas_*.txt` (uno o más por vendedor).
  Incluye `createSalesMenFile`, `createProductsFile` y `createSalesManInfoFile`.
- `src/main.java` — lee esos archivos y crea:
  - `reporte_vendedores.csv`: `Nombre;DineroRecaudado`, de mayor a menor.
  - `reporte_productos.csv`: `Nombre;Precio`, ordenado por cantidad vendida (mayor a menor).
  - `errores_detectados.txt`: solo si encuentra datos erróneos o incoherentes.

Ningún programa solicita información al usuario y ambos informan por consola si
terminaron con éxito o con error.

## Cómo ejecutar en Eclipse

1. `File > Import… > General > Existing Projects into Workspace` y elegir esta carpeta.
2. Clic derecho sobre `GenerateInfoFiles.java` → `Run As > Java Application`.
3. Refrescar el proyecto (F5).
4. Clic derecho sobre `main.java` → `Run As > Java Application`.

Los archivos se crean en la carpeta raíz del proyecto.

## Puntos extra implementados

- **a)** Más de un archivo de ventas por vendedor (agrupados por documento, no por nombre de archivo).
- **c)** Detección de datos erróneos: líneas mal formadas, productos inexistentes,
  cantidades o precios negativos y vendedores no registrados; se omiten y se
  registran en `errores_detectados.txt`.

## Otros archivos

- `conslusion.txt` — resumen de lo aprendido, aplicaciones profesionales y dificultades.
