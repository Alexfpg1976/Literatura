package com.alex.literatura.principal;
import com.alex.literatura.config.ConsumoApi;
import com.alex.literatura.config.ConvertirDatos;
import com.alex.literatura.Modelos.Autor;
import com.alex.literatura.Modelos.Libro;
import com.alex.literatura.Modelos.LibrosRespuestaApi;
import com.alex.literatura.Records.DatosLibro;
import com.alex.literatura.Interface.iAutorRepository;
import com.alex.literatura.Interface.iLibroRepository;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class Principal {

    private Scanner sc = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConvertirDatos convertir = new ConvertirDatos();
    private static String API_BASE = "https://gutendex.com/books/?search=";
    //var json = consumoApi.obtenerDatos("https://gutendex.com/books/?ids=1513");
    //var json = consumoApi.obtenerDatos("https://gutendex.com/books/?search=Romeo%20and%20Juliet");
    private List<Libro> datosLibro = new ArrayList<>();
    private iLibroRepository libroRepository;
    private iAutorRepository autorRepository;

    public Principal(iLibroRepository libroRepository, iAutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void consumo() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    
                    |***************************************************|
                    |*****   BIENVENIDO A LA BIBLIOTECA VIRTUAL   ******|
                    |***************************************************|
                    
                    1 - BUSCAR LIBRO POR NOMBRE
                    2 - LISTAR LIBROS REGISTRADOS
                    3 - LISTAR AUTORES REGISTRADOS
                    4 - BUSCAR AUTORES POR AÑO
                    5 - BUSCAR LIBROS POR IDIOMA
                    6 - TOP 10 DE LOS LIBROS MAS DESCARGADOS
                    
                    
                    
                    0 - SALIR
                    
                    INGRESE UNA OPCIÓN :
                    
                    """;

            try {
                System.out.println(menu);
                opcion = sc.nextInt();
                sc.nextLine();
            } catch (InputMismatchException e) {

                System.out.println("|****************************************|");
                System.out.println("|  POR FAVOR, INGRESA UN NUMERO VALIDO.  |");
                System.out.println("|****************************************|\n");
                sc.nextLine();
                continue;
            }


            switch (opcion) {
                case 1:
                    buscarLibroEnLaWeb();
                    break;
                case 2:
                    librosBuscados();
                    break;
                case 3:
                    Autoresbuscados();
                    break;
                case 4:
                    buscarAutoresPorAnio();
                    break;
                case 5:
                    buscarLibrosPorIdioma();
                    break;
                case 6:
                    top10LibrosMasDescargados();
                    break;





                case 0:
                    opcion = 0;
                    System.out.println("|********************************|");
                    System.out.println("|  FINALIZANDO APLICACION!.....  |");
                    System.out.println("|********************************|\n");
                    break;
                default:
                    System.out.println("|*********************|");
                    System.out.println("|  OPCION INCORRECTA. |");
                    System.out.println("|*********************|\n");
                    System.out.println("INTENTA NUEVAMENTE");
                    consumo();
                    break;
            }
        }
    }

    private Libro getDatosLibro() {
        System.out.println("INGRESA EL NOMBRE DEL LIBRO QUE DESEAS BUSCAR Y ANEXAR A LA BASE DE DATOS: ");
        var nombreLibro = sc.nextLine().toLowerCase();
        var json = consumoApi.obtenerDatos(API_BASE + nombreLibro.replace(" ", "%20"));
        //System.out.println("JSON INICIAL: " + json);
        LibrosRespuestaApi datos = convertir.convertirDatosJsonAJava(json, LibrosRespuestaApi.class);

        if (datos != null && datos.getResultadoLibros() != null && !datos.getResultadoLibros().isEmpty()) {
            DatosLibro primerLibro = datos.getResultadoLibros().get(0); // Obtener el primer libro de la lista
            return new Libro(primerLibro);
        } else {
            System.out.println("NO SE ENCONTRARON RESULTADOS.");
            return null;
        }
    }


    private void buscarLibroEnLaWeb() {
        Libro libro = getDatosLibro();

        if (libro == null) {
            System.out.println("NO SE ENCONTRO EL LIBRO DESEADO");
            return;
        }

        //datosLibro.add(libro);
        try {
            boolean libroExists = libroRepository.existsByTitulo(libro.getTitulo());
            if (libroExists) {
                System.out.println("EL LIBRO YA EXISTE EN LA BASE DE DATOS!");
            } else {
                libroRepository.save(libro);
                System.out.println(libro.toString());
            }
        } catch (InvalidDataAccessApiUsageException e) {
            System.out.println("NO SE PUEDE PERSISTIR EL LIBRO BUSCADO!");
        }
    }

    @Transactional(readOnly = true)
    private void librosBuscados() {
        //datosLibro.forEach(System.out::println);
        List<Libro> libros = libroRepository.findAll();
        if (libros.isEmpty()) {
            System.out.println("NO SE ENCONTRARON LIBROS EN LA BASE DE DATOS.");
        } else {
            System.out.println("LIBROS ENCONTRADOS EN LA BASE DE DATOS :");
            for (Libro libro : libros) {
                System.out.println(libro.toString());
            }
        }
    }
    @Transactional(readOnly = true)
    private void Autoresbuscados() {
        List<Autor> autores = autorRepository.findAll();

        if (autores.isEmpty()) {
            System.out.println("NO SE ENCONTRARON AUTORES EN LA BASE DE DATOS.");
        } else {
            System.out.println("ESTOS SON LOS AUTORES ENCONTRADOS EN LA BASE DE DATOS :");
            for (Autor autor : autores) {
                System.out.println(autor.toString());

            }
        }
    }


    private void buscarLibrosPorIdioma() {
        System.out.println("INGRESE EL IDIOMA QUE QUIERE BUSCAR : \n");
        System.out.println("|**************** IDIOMAS  *************|");
        System.out.println("|  Opción - es : Libros en ESPAÑOL.     |");
        System.out.println("|  Opción - en : Libros en INGLES.      |");
        System.out.println("|  Opción - fr : Libros en FRANCES.     |");
        System.out.println("|  Opción - pt : Libros en PORTUGUES.   |");
        System.out.println("|***************************************|\n");

        var idioma = sc.nextLine();
        List<Libro> librosPorIdioma = libroRepository.findByIdioma(idioma);

        if (librosPorIdioma.isEmpty()) {
            System.out.println("NO SE ENCONTRARON LIBROS CON ESTE IDIOMA EN LA BASE DE DATOS.");
        } else {
            System.out.println("LIBROS ENCONTRADOS EN LA BASE DE DATOS CON EL IDIOMA ESCOGIDO:");
            for (Libro libro : librosPorIdioma) {
                System.out.println(libro.toString());
            }
        }

    }

    private void buscarAutoresPorAnio() {
//        //BUSCAR AUTORES POR ANIO

        System.out.println("INDICA EL AÑO PARA CONSULTAR AUTORES DE ESTA EPOCA : \n");
        var anioBuscado = sc.nextInt();
        sc.nextLine();

        List<Autor> autoresVivos = autorRepository.findByCumpleaniosLessThanOrFechaFallecimientoGreaterThanEqual(anioBuscado, anioBuscado);

        if (autoresVivos.isEmpty()) {
            System.out.println("NO SE ENCONTRARON AUTORES DE ESTA EPOCA " + anioBuscado + ".");
        } else {
            System.out.println("LOS AUTORES QUE CORRESPONDEN A ESTA EPOCA SON : " + anioBuscado + " son:");
            Set<String> autoresUnicos = new HashSet<>();

            for (Autor autor : autoresVivos) {
                if (autor.getCumpleanios() != null && autor.getFechaFallecimiento() != null) {
                    if (autor.getCumpleanios() <= anioBuscado && autor.getFechaFallecimiento() >= anioBuscado) {
                        if (autoresUnicos.add(autor.getNombre())) {
                            System.out.println("AUTOR: " + autor.getNombre());
                        }
                    }
                }
            }
        }
    }

        private void top10LibrosMasDescargados(){
       List<Libro> top10Libros = libroRepository.findTop10ByTituloByCantidadDescargas();
        if (!top10Libros.isEmpty()){
            int index = 1;
            for (Libro libro: top10Libros){
                System.out.printf("Libro %d: %s Autor: %s Descargas: %d\n",
                        index, libro.getTitulo(), libro.getAutores().getNombre(), libro.getCantidadDescargas());
                index++;
            }
        }
    }

}

