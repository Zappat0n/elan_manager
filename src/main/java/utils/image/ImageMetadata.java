package utils.image;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.*;
import javax.imageio.stream.*;
import javax.imageio.metadata.*;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

public class ImageMetadata {
    private final HashMap<String, String> data;

    public ImageMetadata(File file) {
        data = new HashMap<>();
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(file);
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (readers.hasNext()) {
                // pick the first available ImageReader
                ImageReader reader = readers.next();
                // attach source to the reader
                reader.setInput(iis, true);
                // read metadata of first image
                IIOMetadata metadata = reader.getImageMetadata(0);
                String[] names = metadata.getMetadataFormatNames();
                int length = names.length;
                for (String name : names) {
                    System.out.println("Format name: " + name);
                    Node node = metadata.getAsTree(name);
                    NodeList list = node.getChildNodes();
                    for (int j = 0; j < list.getLength(); j++) {
                        Node nod = list.item(j);
                        if (nod.hasChildNodes()) {
                            NodeList lis = nod.getChildNodes();
                            for (int k = 0; k < lis.getLength(); k++) {
                                Node no = lis.item(j);
                                if (no.hasChildNodes()) {
                                    NodeList li = nod.getChildNodes();
                                    for (int l = 0; l < li.getLength(); l++) {
                                        Node n = li.item(l);
                                        addNoteAttributes(n);
                                    }
                                }
                                addNoteAttributes(no);
                            }
                        }
                        addNoteAttributes(nod);
                    }
                }
            }
        }
        catch (Exception e) {e.printStackTrace();}
    }

    private void addNoteAttributes(Node n){
        if (!n.hasAttributes()) return;
        NamedNodeMap map = n.getAttributes();
        for (int i = 0; i < map.getLength(); i++) {
            Node node = map.item(i);
            data.put(node.getNodeName(), node.getNodeValue());
        }
    }

    public String getData(String name){
        return data.get(name);
    }
}
