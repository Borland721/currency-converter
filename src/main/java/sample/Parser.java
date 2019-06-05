package sample;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    public List<CbrValute> getCbrExchange(File file) {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(file.getName(), new FileInputStream(file));

            ArrayList<CbrValute> valutes = new ArrayList<>();

            CbrValute valute = null;
            while (reader.hasNext()) {
                reader.next();
                if (reader.isStartElement()) {
                    String localName = reader.getLocalName();
                    switch (localName) {
                        case "Valute":
                            valute = new CbrValute();
                            valute.setId(reader.getAttributeValue(0));
                            break;
                        case "NumCode":
                            valute.setNumCode(Integer.parseInt(reader.getElementText()));
                            break;
                        case "CharCode":
                            valute.setCharCode(reader.getElementText());
                            break;
                        case "Nominal":
                            valute.setNominal(Integer.parseInt(reader.getElementText()));
                            break;
                        case "Name":
                            valute.setName(reader.getElementText());
                            break;
                        case "Value":
                            valute.setValue(Float.parseFloat(reader.getElementText().replace(",", ".")));
                            break;
                    }
                } else if (reader.isEndElement() && reader.getLocalName().equals("Valute")) {
                    valutes.add(valute);
                }
            }
            reader.close();
            return valutes;
        } catch (XMLStreamException | FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getDate(File file) {
        String date = null;
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(file.getName(), new FileInputStream(file));
            if (reader.hasNext()) {
                reader.next();
                System.out.println(reader.getName());
                date = reader.getAttributeValue(0);
            }
        } catch (FileNotFoundException | XMLStreamException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String requestQuotationDynamics(String urlString, String valuteId) {
        try {
            URL url = new URL(urlString + valuteId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            int responseCode = connection.getResponseCode();
            System.out.println("Response: " + responseCode);
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer responseBuffer = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                responseBuffer.append(inputLine);
                System.out.println(inputLine);
            }
            br.close();
            return responseBuffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<CbrRecord> getQuotationDynamics(String xmlResponse) {
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xmlResponse));

            ArrayList<CbrRecord> records = new ArrayList<>();

            CbrRecord record = null;
            while (reader.hasNext()) {
                reader.next();
                if (reader.isStartElement()) {
                    String localName = reader.getLocalName();
                    switch (localName) {
                        case "Record":
                            record = new CbrRecord();
                            record.setDate(reader.getAttributeValue(0));
                            record.setId(reader.getAttributeValue(1));
                            break;
                        case "Nominal":
                            record.setNominal(Integer.parseInt(reader.getElementText()));
                            break;
                        case "Value":
                            record.setValue(Float.parseFloat(reader.getElementText().replace(",", ".")));
                            break;
                    }
                } else if (reader.isEndElement() && record != null) {
                    records.add(record);
                }
            }
            reader.close();
            return records;
        } catch (XMLStreamException e) {
            e.printStackTrace();
            return null;
        }
    }
}
