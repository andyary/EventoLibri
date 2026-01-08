//package it.polimi.eventolibri.View;
//
//import it.polimi.eventolibri.Model.Libro;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//import static org.assertj.core.api.Assertions.*;
//
//class LibroViewTest {
//
//    private static LibroView view;
//
//    @BeforeAll
//    static void init() {
//        view = new LibroView();
//    }
//
//    @Test
//    void wildcardToRegex_starsAndLowercase() {
//        String regex = view.wildcardToRegex("Gr*ndE");
//        assertThat(regex).isEqualTo("gr.*nde");
//        Pattern p = Pattern.compile(regex);
//        assertThat(p.matcher("grande").matches()).isTrue();
//        assertThat(p.matcher("grXYZnde").matches()).isTrue();
//    }
//
//    @Test
//    void wildcardToRegex_escapesRegexMetaChars() {
//        // dot and question mark must be escaped, star -> .*
//        String regex = view.wildcardToRegex("a.b?*");
//        assertThat(regex).startsWith("a\\.b\\?");
//        assertThat(regex.endsWith(".*")).isTrue();
//        Pattern p = Pattern.compile(regex);
//        assertThat(p.matcher("a.b?foo").matches()).isTrue();
//    }
//
//    @Test
//    void buildPatternFromFilter_nullOrBlank_returnsNull() {
//        assertThat(view.buildPatternFromFilter(null)).isNull();
//        assertThat(view.buildPatternFromFilter("")).isNull();
//        assertThat(view.buildPatternFromFilter("    ")).isNull();
//    }
//
//    @Test
//    void buildPatternFromFilter_substringSearch_whenNoWildcard() {
//        Pattern p = view.buildPatternFromFilter("ciao");
//        assertThat(p).isNotNull();
//        // should match substring anywhere
//        assertThat(p.matcher("xxciaoxx").matches()).isTrue();
//        assertThat(p.matcher("ciao").matches()).isTrue();
//    }
//
//    @Test
//    void buildPatternFromFilter_matches_withWildcard() {
//        Pattern p = view.buildPatternFromFilter("grande*");
//        assertThat(p).isNotNull();
//        // should match titles containing or starting with 'grande'
//        assertThat(p.matcher("il grande titolo").matches()).isTrue();
//        assertThat(p.matcher("grandezza").matches()).isTrue();
//    }
//
//    @Test
//    void buildPatternFromFilter_escapesPlusAndOtherMetas() {
//        Pattern p = view.buildPatternFromFilter("c++");
//        assertThat(p).isNotNull();
//        // should match strings that include literal 'c++'
//        assertThat(p.matcher("uso di c++ nella descrizione").matches()).isTrue();
//    }
//
//    @Test
//    void filteredPredicate_simulation_multipleLibri() {
//        List<Libro> libri = List.of(
//                new Libro("Java per tutti", 120, "l1", "Autore A", 1),
//                new Libro("Il grande romanzo", 200, "l2", "Autore B", 2),
//                new Libro("Programmare in C++", 90, "l3", "Autore C", 3),
//                new Libro("Storia", 60, "l4", "Autore D", 4)
//        );
//
//        Pattern p = view.buildPatternFromFilter("grande*");
//        List<Libro> filtered = libri.stream()
//                .filter(l -> p.matcher(l.getTitolo() != null ? l.getTitolo().toLowerCase() : "").matches())
//                .collect(Collectors.toList());
//
//        assertThat(filtered).hasSize(1);
//        assertThat(filtered.get(0).getTitolo()).isEqualTo("Il grande romanzo");
//    }
//}
