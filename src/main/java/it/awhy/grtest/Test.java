/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.awhy.grtest;

import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

/**
 *
 * @author giulia
 */
public class Test {

    private static final String gr_NS = "http://purl.org/goodrelations/v1";
    private static final String business_NS = "http://www.awhy.com/awhyOntology/business-entity";
    private static final String schema_NS = "http://schema.org/";
    private static final String rdfs_NS="http://www.w3.org/2000/01/rdf-schema";

    public static void main(String[] args) {

        // preparazione per il parsing dell'XML
        File xml = new File("parafarmaciaREDUCED.xml");
        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = null;
        try {
            document = saxBuilder.build(xml);
        } catch (JDOMException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        Namespace ns = Namespace.getNamespace("g", "http://base.google.com/ns/1.0");

        // preparazione per la scrittura in Good Relation
        PrefixManager grNS = new DefaultPrefixManager(null, null, gr_NS);
        PrefixManager businessNS = new DefaultPrefixManager(null, null, business_NS);
        PrefixManager schemaNS = new DefaultPrefixManager(null, null, schema_NS);
        PrefixManager rdfsNS = new DefaultPrefixManager(null, null, rdfs_NS); 
        
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology gr = null;
        File ontoFile = null;
        try {
            ontoFile = new File("Business-entity.owl");
            gr = manager.loadOntologyFromOntologyDocument(ontoFile);
        } catch (OWLOntologyCreationException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        OWLDataFactory factory = manager.getOWLDataFactory();

        // recupero la classe che mi interessa
        OWLClass IndividualClass = factory.getOWLClass("#ProductOrServiceModel", grNS);
        OWLClass BrandClass = factory.getOWLClass("#Brand", grNS);
        OWLClass BusinessEntityClass = factory.getOWLClass("#BusinessEntity", grNS);
        OWLClass PriceSpecs = factory.getOWLClass("#UnitPriceSpecification", grNS);
        OWLClass DeliverySpecs = factory.getOWLClass("#DeliveryChargeSpecification", grNS);
        OWLClass OrganizationClass = factory.getOWLClass("Organization", schemaNS);
        OWLClass OfferingClass = factory.getOWLClass("#Offering", grNS);
        OWLClass LocationClass = factory.getOWLClass("#Location", grNS);

        Element rssElement = document.getRootElement();
        Element channel = rssElement.getChild("channel");
        OWLNamedIndividual organization_individual = factory.getOWLNamedIndividual("#" + cleanData(channel.getChildText("title")), businessNS);
        addInstanceToClass(manager, factory, OrganizationClass, organization_individual, gr);
        for (Element item : rssElement.getChild("channel").getChildren()) {
            if (item.getChild("title") != null) {
                // creo l'individuo nel namespace che voglio
                OWLNamedIndividual product_individual = factory.getOWLNamedIndividual("#" + cleanData(item.getChildText("title")), businessNS);
                OWLNamedIndividual brand = factory.getOWLNamedIndividual("#" + cleanData(item.getChildText("brand", ns)), businessNS);
                OWLNamedIndividual price_specs = factory.getOWLNamedIndividual("#UnitPriceSpecification_" + cleanData(item.getChildText("title")), businessNS);
                OWLNamedIndividual delivery_specs = factory.getOWLNamedIndividual("#DeliveryChargeSpecification_" + cleanData(item.getChildText("title")), businessNS);
                OWLNamedIndividual offering = factory.getOWLNamedIndividual("#Offering_" + cleanData(item.getChildText("title")), businessNS);
              
                
                // scrivo l'asserzione nell'ontologia
                addInstanceToClass(manager, factory, IndividualClass, product_individual, gr);
                addInstanceToClass(manager, factory, BrandClass, brand, gr);
                addInstanceToClass(manager, factory, PriceSpecs, price_specs, gr);
                addInstanceToClass(manager, factory, DeliverySpecs, delivery_specs, gr);
                addInstanceToClass(manager, factory, OfferingClass, offering, gr);
                
                // aggiungo le data-property 
                addDataPropertyAxiom(manager, factory, "status", product_individual, item.getChildText("availability", ns), grNS, gr, "string");
                addDataPropertyAxiom(manager, factory, "hasCurrencyValue", price_specs, getCurrencyValue(item.getChildText("price", ns)), grNS, gr, "float");
                addDataPropertyAxiom(manager, factory, "hasCurrency", price_specs, getCurrency(item.getChildText("price", ns)), grNS, gr, "string");
                addDataPropertyAxiom(manager, factory, "isDefinedBy", brand, cleanData(item.getChildText("brand", ns)), rdfsNS, gr, "string");
                
                for (Element ship : item.getChild("shipping", ns).getChildren()) {
                    if ((ship.getName()).equals("country")) {
                        System.out.println("Luogo di spedizione: " + ship.getText());
                        //OWLNamedIndividual location = factory.getOWLNamedIndividual("#" + ship.getText(), businessNS);
                        //addInstanceToClass(manager, factory, LocationClass, location, gr);
                        addDataPropertyAxiom(manager, factory, "eligibleRegions", offering, ship.getText(), grNS, gr, "string");
                        
                        }
                    if ((ship.getName()).equals("price")) {
                        System.out.println("Prezzo di spedizione: " + ship.getText());
                        addDataPropertyAxiom(manager, factory, "hasCurrencyValue", delivery_specs, getCurrencyValue(ship.getText()), grNS, gr, "float");
                        addDataPropertyAxiom(manager, factory, "hasCurrency", delivery_specs, getCurrency(ship.getText()), grNS, gr, "string");
                    }
                }

                // Aggiungo le object-property
                addObjectPropertyAxiom(manager, factory, gr, "offers(0..*)", organization_individual, offering, grNS);
                addObjectPropertyAxiom(manager, factory, gr, "includes(0..1)", offering, product_individual, grNS);
                addObjectPropertyAxiom(manager, factory, gr, "hasPriceSpecification(0..*)", offering, price_specs, grNS);
                addObjectPropertyAxiom(manager, factory, gr, "hasPriceSpecification(0..*)", offering, delivery_specs, grNS);
                addObjectPropertyAxiom(manager, factory, gr, "hasManufacturer", product_individual, brand, grNS);

                System.out.println(rssElement.getChild("channel").getChildText("title"));
               // System.out.println("Nome del prodotto: " + item.getChildText("title"));
               // System.out.println("Descrizione del prodotto: " + item.getChildText("description"));
               // System.out.println("Brand del prodotto: " + item.getChildText("brand", ns));
               // System.out.println("Prezzo del prodotto: " + item.getChildText("price", ns));
               // System.out.println("Prezzo del prodotto scontato: " + item.getChildText("sale_price", ns));
               // System.out.println("Disponibilità in magazzino: " + item.getChildText("availability", ns));
               // System.out.println("Categoria del prodotto: " + item.getChildText("google_product_category", ns));
               // System.out.println("Numero MPN:" + item.getChildText("mpn", ns));

               // System.out.println("---------------------------------------");
            }
        }

        saveOntology(manager, gr, ontoFile);
        makeQuery(manager, gr);
    }

    private static void makeQuery(OWLOntologyManager m, OWLOntology o) {
        StructuralReasonerFactory factory = new StructuralReasonerFactory();
        OWLReasoner reasoner = factory.createReasoner(o);
        QueryEngine engine = QueryEngine.create(m, reasoner);
        String q = "PREFIX goodrelations: <" + gr_NS + "#>\n"
                + "PREFIX business: <" + business_NS + "#>\n"
                + "SELECT ?subclass WHERE { PropertyValue(?subclass, Aftir_shampoo_150ml , goodrelations:Includes(0..1)}";

        String q1 = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
                + "PREFIX gr:<" + business_NS + "#>\n"
                + "PREFIX business: <" + business_NS + "#>\n"
                + "SELECT ?s ?p ?c\n"
                + "WHERE {\n"
                + "?s a gr:Offering.\n"
                + "?s gr:hasPriceSpecification ?ps.\n"
                + "?ps gr:hasCurrencyValue ?p.\n"
                + "?ps gr:hasCurrency ?c.\n"
                + "FILTER ((regex(?c, \"EUR\") && ?p >\"5\"^^xsd:float && ?p <\"10\"^^xsd:float)}";
               
              
                
         
      //  processQuery(engine, q);
    }

    private static void processQuery(QueryEngine qe, String q) {
        try {
            Query query = Query.create(q);
            System.out.println("Eseguo la query: " + q);
            System.out.println("---------------------------");
            try {
                String result = qe.execute(query).toString();
                String[] lines = result.split("\n");
                for (String line : lines) {
                    //System.out.println("Questa è la linea: "+line);
                    String[] parts = line.split("#");
                    String toprint = parts[1].replace("_", " ");
                    System.out.println("Entity: " + toprint);
                }

                //XMLOutputter out = new XMLOutputter();
                //out.output(result.toXML(), System.out);             
                //System.out.println(result);
            } catch (QueryEngineException ex) {
                Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (QueryParserException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void addObjectPropertyAxiom(OWLOntologyManager m, OWLDataFactory df, OWLOntology o, String objProp, OWLNamedIndividual src, OWLNamedIndividual trg, PrefixManager ns) {
        OWLObjectProperty p = df.getOWLObjectProperty("#" + objProp, ns);
        OWLObjectPropertyAssertionAxiom propertyAssertion = df.getOWLObjectPropertyAssertionAxiom(p, src, trg);
        m.addAxiom(o, propertyAssertion);
    }

    private static void addInstanceToClass(OWLOntologyManager m, OWLDataFactory df, OWLClass c, OWLNamedIndividual i, OWLOntology o) {
        OWLClassAssertionAxiom ax = df.getOWLClassAssertionAxiom(c, i);
        m.addAxiom(o, ax);
    }

    private static void addDataPropertyAxiom(OWLOntologyManager m, OWLDataFactory df, String dataProp, OWLNamedIndividual src, Object trg, PrefixManager ns, OWLOntology o, String type) {
        OWLDataProperty p = df.getOWLDataProperty("#" + dataProp, ns);
        OWLDataPropertyAssertionAxiom dataPropertyAssertion = null;
        if (type.equals("float")) {
            float _trg = Float.parseFloat(trg.toString());
            dataPropertyAssertion = df.getOWLDataPropertyAssertionAxiom(p, src, _trg);
        } else {
            if (type.equals("string")) {
                String _trg = trg.toString();
                dataPropertyAssertion = df.getOWLDataPropertyAssertionAxiom(p, src, _trg);
            }
        }
        m.addAxiom(o, dataPropertyAssertion);
    }

    private static void saveOntology(OWLOntologyManager m, OWLOntology o, File f) {
        try {
            // salvo la nuova ontologia istanziata su un nuovo file
            m.saveOntology(o, IRI.create(f.toURI()));
        } catch (OWLOntologyStorageException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String cleanData(String s) {
        String cleanS = "";
        cleanS = s.replaceAll("'", "");
        cleanS = cleanS.replaceAll("\\+", "");
        cleanS = cleanS.replaceAll("\\.", "");
        cleanS = cleanS.replaceAll(" ", "_");
        return cleanS;
    }

    private static float getCurrencyValue(String s) {
        return Float.parseFloat(s.split(" ")[0]);
    }

    private static String getCurrency(String s) {
        return s.split(" ")[1];
    }

}
