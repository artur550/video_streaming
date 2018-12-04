import java.io.*;import java.net.URL;import java.util.regex.Matcher;import java.util.regex.Pattern;import java.util.ArrayList;import java.nio.charset.Charset;import java.nio.charset.StandardCharsets;import java.nio.file.Files;import java.nio.file.Paths;import java.util.HashMap;import java.util.List;import java.util.Map;import com.google.gson.Gson;import com.google.gson.JsonArray;import com.google.gson.JsonObject;import jdk.internal.util.xml.impl.Input;import javax.swing.plaf.synth.SynthTextAreaUI;public class Test {    public static void main(String[] args) throws IOException {        final URL url = new URL(                "https://dev-db1.workfusion.pg.com:8443/s3-otc-eu-italy-adaptmiamor/orders/3e4e7a94eb0ea8280e20b2d52763d18c.txt");        final File file = new File("Y:\\OTC-EU-Manual_Order_Creation_WF\\SE\\Italy\\DEV\\Process Inputs\\Orders\\txt order only\\FARVIMA\\" +                "ordine Roma licenza.txt");// MAIN BODY:        InputStream inputStream = new FileInputStream(file);//        InputStream inputStream = url.openConnection().getInputStream();        byte[] bytes = new byte[inputStream.available()];        inputStream.read(bytes);        String text = new String(bytes,"UTF-8");        System.out.println(text);        Client client = new Client(text.toString().trim());        OrderDetails orderDetails = new OrderDetails(text.toString().trim());        OrderLine orderLine = new OrderLine(text.toString().trim());        Map<String, List> product_information = new HashMap<>();        String purchase_order = orderDetails.getOrderNumber();        String customer_name = client.getName();        String city = client.getCity();        String zip_code = client.getZip();        String street_address = client.getStreet();        String request_delivery_date = orderDetails.getRequestDeliveryDate();        product_information.put("product_information", orderLine.extractProducts());        String product_information_json = new Gson().toJson(product_information);        boolean missed_mandatory_field = false;        if (purchase_order.isEmpty()                || customer_name.isEmpty()                || city.isEmpty()                || zip_code.isEmpty()                || street_address.isEmpty()                || request_delivery_date.isEmpty()                || product_information.isEmpty()) {            missed_mandatory_field = true;        }        System.out.println(purchase_order + "\n" +                        customer_name + "\n" +                        city + "\n" +                        zip_code + "\n" +                        street_address + "\n" +                        request_delivery_date + "\n" +                        missed_mandatory_field);        JsonObject json = new Gson().fromJson(product_information_json, JsonObject.class);        JsonArray jsonArray = json.get("product_information").getAsJsonArray();        int i = 1;        jsonArray.forEach(cons->{            System.out.print("QTY: "+cons.getAsJsonObject().get("quantity").getAsString());            System.out.println(",\tNR: " + cons.getAsJsonObject().get("product_number").getAsString());        });    }}class OrderLine {    private String content;    public OrderLine(String text) {        content = text;    }    public List<Map<String, String>> extractProducts() throws IOException {        List<Map<String, String>> productList = new ArrayList<>();        BufferedReader bufReader = new BufferedReader(new StringReader(content));        String line = "";        if (content.contains("N U M E R O   O R D I N E:")) {            Pattern pattern = Pattern.compile(                    "^(\\d+)\\s+(.+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+([\\d,?]+)");            while ((line = bufReader.readLine()) != null) {                Matcher matcher = pattern.matcher(line.trim());                if (matcher.find()) {                    Map<String, String> productItem = new HashMap<>();                    productItem.put("product_number", matcher.group(1));                    productItem.put("quantity", matcher.group(4));                    productList.add(productItem);                }            }        } else {            Pattern pattern = Pattern.compile(                    "^(\\d+)\\s+(.+)\\s+(\\d+)\\s+(\\d+)\\s+([\\d,?]+)(\\s+([\\d,?]+)\\s+([\\d,?]+))?");            while ((line = bufReader.readLine()) != null) {                Matcher matcher = pattern.matcher(line.trim());                if (matcher.find()) {                    Map<String, String> productItem = new HashMap<>();                    productItem.put("product_number", matcher.group(4));                    productItem.put("quantity", matcher.group(3));                    productList.add(productItem);                }            }        }        return productList;    }}class Client {    private String name;    private String city;    private String street;    private String zip;    public Client(String text) {        name = setName(text);        street = setStreet(text);        city = setCity(text);        zip = setZip(text);    }    public String getName() {        return name;    }    public String getCity() {        return city;    }    public String getStreet() {        return street;    }    public String getZip() {        return zip;    }    private String setName(String text) {        Pattern regexp = Pattern.compile("(.*)\\s+((S.r.l.)|(SpA)|(S.P.A.)|(SPA)|(Scrl))");        Matcher matcher = regexp.matcher(text);        if (matcher.find()) {            return matcher.group(1);        }        return "";    }    private String setStreet(String text) {        String line;        int index;        if (text.startsWith("Mitt.")) {            line = getNthLine(text, 2);            index = line.indexOf("   ");            return line.substring(0, index);        } else if (text.startsWith("**** BUONO ORDINE ****")) {            line = getNthLine(text, 3);            index = line.indexOf("   ");            return line.substring(0, index);        } else if (text.contains("N U M E R O   O R D I N E:")) {            return line = getNthLine(text, 3);        } else {            return "";        }    }    private String setZip(String text) {        String line;        Pattern regexp = Pattern.compile("^(\\d{5})\\s+([\\w,\\.]+(?:[\\s-][\\w]+)*)(\\s+[(]?\\w{2}[)]?)");        Matcher matcher = regexp.matcher("");        if (text.startsWith("Mitt.")) {            line = getNthLine(text, 3);            matcher.reset(line);            if (matcher.find()) {                return matcher.group(1);            }        } else if (text.startsWith("**** BUONO ORDINE ****")) {            line = getNthLine(text, 4);            matcher.reset(line);            if (matcher.find()) {                return matcher.group(1);            }        } else if (text.contains("N U M E R O   O R D I N E:")) {            line = getNthLine(text, 4);            matcher.reset(line);            if (matcher.find()) {                return matcher.group(1);            }        } else {            return "";        }        return "";    }    private String setCity(String text) {        String line;        Pattern regexp = Pattern.compile("^(\\d{5})\\s+([\\w,\\.]+(?:[\\s-][\\w]+)*)(\\s+[(]?\\w{2}[)]?)");        Matcher matcher = regexp.matcher("");        if (text.startsWith("Mitt.")) {            line = getNthLine(text, 3);            matcher.reset(line);            if (matcher.find()) {                return matcher.group(2);            }        } else if (text.startsWith("**** BUONO ORDINE ****")) {            line = getNthLine(text, 4);            matcher.reset(line);            if (matcher.find()) {                return matcher.group(2);            }        } else if (text.contains("N U M E R O   O R D I N E:")) {            line = getNthLine(text, 4);            matcher.reset(line);            if (matcher.find()) {                return matcher.group(2);            }        } else {            return "";        }        return "";    }    private String getNthLine(String text, int lineNumber) {        String newLine = System.getProperty("line.separator");        int newLineIndex = text.indexOf(newLine);        String line = text;        for (int i = 0; i < lineNumber; i++) {            line = line.substring(newLineIndex);            line = line.trim();            newLineIndex = line.indexOf(newLine);        }        return line.substring(0, newLineIndex);    }}class OrderDetails {    private String requestDeliveryDate;    private String orderNumber;    public OrderDetails(String text) {        orderNumber = setOrderNumber(text);        requestDeliveryDate = setRequestDeliveryDate(text);    }    private String setRequestDeliveryDate(String text) {        if (text.contains("N U M E R O   O R D I N E:")) {            Pattern regexp = Pattern.compile("(Consegna: )(\\d\\d\\.\\d\\d\\.\\d{4})\\s+(.*)");            Matcher matcher = regexp.matcher(text);            if (matcher.find()) {                return matcher.group(2);            }        }        return "";    }    private String setOrderNumber(String text) {        if (text.contains("N U M E R O   O R D I N E:")) {            Pattern regexp = Pattern.compile("(N U M E R O   O R D I N E: )([\\d,\\w]{2}/\\d+)\\s+(D A T A:.*)");            Matcher matcher = regexp.matcher(text);            if (matcher.find()) {                return matcher.group(2);            }        } else {            Pattern regexp = Pattern.compile("(.*)\\s+(Ordine[:]?)\\s+(\\d+)(\\.\\d+)\\s+(.*)");            Matcher matcher = regexp.matcher(text);            if (matcher.find()) {                return matcher.group(3);            }        }        return "";    }    public String getRequestDeliveryDate() {        return requestDeliveryDate;    }    public String getOrderNumber() {        return orderNumber;    }}