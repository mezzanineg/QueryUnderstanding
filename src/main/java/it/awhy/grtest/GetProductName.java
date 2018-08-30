/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.awhy.grtest;


import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author giulia
 */
public class GetProductName {

    public static void main(String[] args) {

        // preparazione per il parsing dell'XML
        File xml = new File("parafarmacia.xml");
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
        Element rssElement = document.getRootElement();
        Element channel = rssElement.getChild("channel");
        for (Element item : rssElement.getChild("channel").getChildren()) {
            if (item.getChild("title") != null) {
               System.out.println(item.getChildText("title"));
            }
        }
    }
}
