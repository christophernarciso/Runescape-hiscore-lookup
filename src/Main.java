
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        if (args == null){
            System.exit(1);
            return;
        }

        String searchPhrase = args[0];
        HashMap<String, Stats> hiscoreDataMap = getHiscoreData(searchPhrase);
        System.out.println("\n" + "Personal scores for: " + searchPhrase);

        for (String data : hiscoreDataMap.keySet()){
            System.out.println("Statistics for: " + data);
            System.out.println("Rank: " + hiscoreDataMap.get(data).getRank());
            System.out.println("Level: " + hiscoreDataMap.get(data).getLevel());
            System.out.println("Experience: " + hiscoreDataMap.get(data).getExp() + "\n");
        }
    }

    public static HashMap<String, Stats> getHiscoreData(final String search){
        HashMap<String, Stats> temp = new HashMap<>();
        try {
            //Jsoup closes the connection by its own, after the request is done
            Document document = Jsoup
                    .connect("https://secure.runescape.com/m=hiscore_oldschool/hiscorepersonal.ws?user1=" + search.replaceAll(" ", "%20"))
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
                    .timeout(5000)
                    .get();
            //Select the first table.
            Element table = document.select("table").get(0);
            //Grab our rows of td from Element table
            Elements rows = table.select("td");
            //Iterator to remove unnecessary data
            Iterator<Element> elementIterator = rows.iterator();
            //List holds our skill type names
            List<String> typesAvailable = new ArrayList<>();

            while (elementIterator.hasNext()) {
                Element node = elementIterator.next();
                String attributesString = node.attributes().toString();
                String text = node.text();
                //Remove unneeded elements
                if (attributesString.contains(" valign=\"top\"") || attributesString.contains(" align=\"left\"")
                        || attributesString.contains(" width=") || attributesString.contains(" colspan=")
                        || text.getBytes().length == 0 || Character.isLetter(text.charAt(0))) {
                    //Add our skill names to the map
                    if (attributesString.contains(" align=\"left\"") && !attributesString.contains(" valign=\"top\"")) {
                        System.out.println("Text available: " + text);
                        System.out.println("Type added: " + text);
                        typesAvailable.add(text);
                        temp.put(text, new Stats(0, 0, 0));
                    }
                    System.out.println("Removed: " + node.attributes());
                    elementIterator.remove();
                }
            }

            String type, rank, level, xp;
            //Loop through our elements and submit innertext to temp
            for (int i = 0, j = 0; i < rows.size() && j < typesAvailable.size(); i += 3, j++) {
                //Get type from typesAvailable
                type = typesAvailable.get(j);
                //Remove commas from our innertext data
                rank = rows.get(i).text().replaceAll(",", "");
                level = rows.get(i + 1).text().replaceAll(",", "");
                xp = rows.get(i + 2).text().replaceAll(",", "");
                //Add to our stats hashmap
                temp.put(type, new Stats(Integer.parseInt(rank), Integer.parseInt(level), Integer.parseInt(xp)));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return temp;
    }

    public static class Stats {
        private int rank;
        private int level;
        private int exp;

        public Stats(int rank, int level, int exp) {
            this.rank = rank;
            this.level = level;
            this.exp = exp;
        }

        public int getRank() {
            return this.rank;
        }

        public int getLevel() {
            return this.level;
        }

        public int getExp() {
            return this.exp;
        }
    }

}
