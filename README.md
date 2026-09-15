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
