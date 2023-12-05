package com.factura.modelo;

import com.boleta.model.AddressDetails;
import com.boleta.model.HeaderDetails;
import com.boleta.model.Product;
import com.boleta.model.ProductTableHeader;
import com.boleta.service.PdfInvoiceCreator;

import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import jnafilechooser.api.JnaFileChooser;

public class GeneradorFacturaPDF {

    String nombreEmpresa;
    String direccionEmpresa;
    String emailEmpresa;
    String descripcionEmpresa;

    public GeneradorFacturaPDF(String nombreEmpresa, String direccionEmpresa, String emailEmpresa, String descripcionEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
        this.direccionEmpresa = direccionEmpresa;
        this.emailEmpresa = emailEmpresa;
        this.descripcionEmpresa = descripcionEmpresa;
    }

    public void generarPDF(String codigoFactura, String empleado, String cliente, String emailCliente, JTable tabla) {
        try {
            String fecha = LocalDate.now().toString();
            String pdfName = codigoFactura + "_" + fecha + ".pdf";
            String direccionDescarga = FileChosserArchivo(pdfName, "pdf", "Factura");

            if (direccionDescarga != null) {

                PdfInvoiceCreator cepdf = new PdfInvoiceCreator(direccionDescarga);
                cepdf.createDocument();

                //Create Header start
                HeaderDetails header = new HeaderDetails();
                header.setInvoiceNo(codigoFactura).setInvoiceDate(fecha).build();
                cepdf.createHeader(header);
                //Header End

                //Create Address start
                AddressDetails addressDetails = new AddressDetails();
                addressDetails
                        .setBillingCompany(nombreEmpresa)
                        .setBillingName(empleado)
                        .setBillingAddress(direccionEmpresa)
                        .setBillingEmail(emailEmpresa)
                        .setShippingName(cliente)
                        .setShippingAddress(emailCliente)
                        .build();

                cepdf.createAddress(addressDetails);
                //Address end

                //Product Start
                ProductTableHeader productTableHeader = new ProductTableHeader();
                cepdf.createTableHeader(productTableHeader);
                List<Product> listaProductos = getProductos(tabla);
                listaProductos = cepdf.modifyProductList(listaProductos);
                cepdf.createProduct(listaProductos);
                //Product End

                //Term and Condition Start
                List<String> TncList = new ArrayList<>();
                TncList.add(descripcionEmpresa);
                String imagePath = "";
                cepdf.createTnc(TncList, false, imagePath);
            }
        } catch (FileNotFoundException | NullPointerException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private List<Product> getProductos(JTable tabla) {
        List<Product> listaProductos = new ArrayList<>();
        try {
            for (int i = 0; i < tabla.getRowCount(); i++) {
                String producto = tabla.getValueAt(i, 0).toString();
                int cantidad = Integer.parseInt(tabla.getValueAt(i, 1).toString());
                float precio = Float.parseFloat(tabla.getValueAt(i, 2).toString());
                Product p = new Product(producto, cantidad, precio);
                listaProductos.add(p);
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return listaProductos;
    }

    public static String FileChosserArchivo(String defaultName, String extension, String descripcionTitulo) {
        JnaFileChooser fileChooser = new JnaFileChooser();
        fileChooser.addFilter(descripcionTitulo, extension);
        fileChooser.setMultiSelectionEnabled(false); // Cambia a true si deseas habilitar la selecci�n m�ltiple
        fileChooser.setMode(JnaFileChooser.Mode.Files); // Cambia el modo seg�n tus necesidades

        // Establece el nombre de archivo por defecto
        if (defaultName != null && !defaultName.isEmpty()) {
            fileChooser.setDefaultFileName(defaultName + "." + extension);
        }

        if (fileChooser.showOpenDialog(null)) {
            String selectedFilePath = fileChooser.getSelectedFile().getAbsolutePath();
//            System.out.println("Archivo seleccionado: " + selectedFilePath);
            return selectedFilePath;
        } else {
//            System.out.println("Selecci�n de archivo cancelada.");
            return null;
        }
    }
}
