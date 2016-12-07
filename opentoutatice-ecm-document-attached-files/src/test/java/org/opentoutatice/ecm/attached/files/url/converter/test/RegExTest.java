package org.opentoutatice.ecm.attached.files.url.converter.test;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import fr.toutatice.ecm.platform.core.helper.ToutaticeDocumentMetadataHelper;

//@RunWith(FeaturesRunner.class)
//@Features(CoreFeature.class)
public class RegExTest {
    
//    @Inject
//    HtmlSanitizerServiceImpl sanitizer;
    
    
    static final String INTERNAL_PICTURE_INDICATOR = "internalPicture";
    static final Pattern PATTERN_NX_INTERNAL_PICTURE = Pattern.compile("(src=\"nxfile/default/)([a-zA-Z0-9[-]&&[^/]]*)(/ttc:images/[.[^\"]]+\")");
    static final Pattern PATTERN_NX_INTERNAL_PICTURE_PARAM = Pattern.compile("([?]conversationId=[.[^\"]]+)");
    static final Pattern PATTERN_INTERNAL_PICTURE = Pattern.compile("(src=\"nxfile/default/)(".concat(INTERNAL_PICTURE_INDICATOR).concat(")").concat("(/ttc:images/[.[^\"]]*\")"));
    
    static final Pattern IMG_SRC = Pattern.compile("([p{L}p{N}.#@$%+&amp;;-_~,?=/!]+|#(w)+)");
    

    @Test
    public void test_nx_internal_picture() {
        String content = "<p><img alt=\"\" class=\"enlargeable\" src=\"nxfile/default/df253d7f-4811-42d7-b853-512df435c1b0/ttc:images/2/file/chat.jpeg\" />ppp</p> \r\n"
                .concat("<p><img alt=\"\" class=\"enlargeable\" src=\"nxfile/default/df253d7f-8811-42d7-b853-512df435c1b0/ttc:images/0/file/rat.jpg?conversationId=0NXMAIN1\" alt=\"\" />pbp</p>");
        
        Matcher matcher = PATTERN_NX_INTERNAL_PICTURE.matcher(content);
        
        StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String g3 = matcher.group(3);
                Matcher paramMatcher = PATTERN_NX_INTERNAL_PICTURE_PARAM.matcher(g3);
                if(paramMatcher.find()){
                    g3 = StringUtils.remove(g3, paramMatcher.group(1));
                }
                matcher.appendReplacement(sb, "$1" + INTERNAL_PICTURE_INDICATOR + g3);
            }
            sb = matcher.appendTail(sb);
        System.out.println(sb.toString());
    }
    
    @Test
    public void test_internal_picture() {
        String content = "<p><img alt=\"\" class=\"enlargeable\" src=\"nxfile/default/internalPicture/ttc:images/2/file/chat.jpeg\" alt=\"\"/>ppp</p> \r \n\n  \r"
                .concat("<p><img alt=\"\" class=\"enlargeable\" src=\"nxfile/default/internalPicture/ttc:images/0/file/chat222222.jpeg\" alt=\"\" />ppp</p>");
        
        Matcher matcher = PATTERN_INTERNAL_PICTURE.matcher(content);
        final String param = "?conversationId=ONXMAIN18";
        
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String g3 = matcher.group(3);
            
            g3 = StringUtils.remove(g3, "\"").concat(param).concat("\"");
            
            matcher.appendReplacement(sb, "$1" + "TTTTT" + g3);
        }
        sb = matcher.appendTail(sb);
        System.out.println(sb.toString());
    } 
    
    static final Pattern TITLE_COPIED = Pattern.compile("(.*)\\((copie |copie)?([0-9]+)?\\)(\\.[a-z]+)?$");
    
    @Test
    public void test_title() {
        String title = "Doc à oim.pdf";
        
        Matcher matcher = TITLE_COPIED.matcher(title);

            if (matcher.find()) {
                StringBuffer sb = new StringBuffer(); 
                String number = matcher.group(3);
                if(number != null){
                    int num = Integer.valueOf(number).intValue() + 1;
                    number =  String.valueOf(num); 
                } else {
                    number = " " + String.valueOf(1);
                }
                matcher.appendReplacement(sb, "$1" +  "(" + "$2" + number + ")" + "$4");
                title =  matcher.appendTail(sb).toString();
            } else {
                String fileExtension = StringUtils.substringAfterLast(title, ".");
                if(StringUtils.isNotEmpty(fileExtension)){
                    fileExtension = ".".concat(fileExtension);
                }
                title = StringUtils.removeEnd(title, fileExtension).concat(" (copie)")
                        .concat(fileExtension);
            }
            
       
            
        System.out.println(title);
    }
    
private static final String ATTACHED_RESOURCE_INDICATOR = "attachedImages";
    
    /** Component internal link pattern. */
    static final Pattern PATTERN_COMPONENT_LINK_OR_RESOURCE = Pattern.compile("(src|href)(=\")([.[^\"]]+\")");

    /** Component internal picture pattern. */
    static final Pattern PATTERN_COMPONENT_ATTACHED_RESOURCE = Pattern.compile("(nxfile/default/)([a-zA-Z0-9[-]&&[^/]]*)(/ttc:images/[.[^\"]]*\")");
    static final Pattern PATTERN_COMPONENT_ATTACHED_RESOURCE_PARAM = Pattern.compile("([?]conversationId=[.[^\"]]+)");

    /** Model internal picture pattern. */
    static final Pattern PATTERN_MODEL_ATTACHED_RESOURCE = Pattern.compile("(nxfile/default/)(".concat(ATTACHED_RESOURCE_INDICATOR).concat(")").concat("(/ttc:images/[.[^\"]]*\")"));
    
    @Test
    public void test_all() {
        String content = "<p><a href=\"web/demox1\"><img alt=\"\" class=\"enlargeable\" src=\"web/bernie-mini3?content=Medium\" /></a></p><p><a href=\"web/demox1\">DemoX</a></p>"
                + "<p><img alt=\"\" class=\"enlargeable\" src=\"nxfile/default/attachedImages/ttc:images/0/file/chat.jpeg\" /></p>";
        
        StringBuffer replacement = new StringBuffer();
        if (content != null) {
            Matcher pictMatcher = PATTERN_COMPONENT_ATTACHED_RESOURCE.matcher(content);
            StringBuffer pictReplacementStack = new StringBuffer();
            while (pictMatcher.find()) {
                String g3 = pictMatcher.group(3);
                Matcher paramMatcher = PATTERN_COMPONENT_ATTACHED_RESOURCE_PARAM.matcher(g3);
                if (paramMatcher.find()) {
                    g3 = StringUtils.remove(g3, paramMatcher.group(1));
                }
                pictMatcher.appendReplacement(pictReplacementStack, "$1" + ATTACHED_RESOURCE_INDICATOR + g3);
            }
            replacement = pictMatcher.appendTail(pictReplacementStack);
            
            Matcher linkMatcher = PATTERN_COMPONENT_LINK_OR_RESOURCE.matcher(replacement.toString());
            StringBuffer linkReplacementStack = new StringBuffer();
            while(linkMatcher.find()){
                // We store URL links prefixed with webapp's name
                String g3 = linkMatcher.group(3);
                String rp3 = g3;
                //if(!StringUtils.startsWith(rp3, "nxfile")){
                    if (!StringUtils.startsWith(g3, "/nuxeo/")) {
                        rp3 = "/nuxeo/".concat(g3);
                    }
                //}
                
                linkMatcher.appendReplacement(linkReplacementStack, "$1" + "$2" + rp3);
            }
            replacement = linkMatcher.appendTail(linkReplacementStack);
            

        }
        System.out.println(replacement.toString());
    } 
    
//    @Test
//    public void test_img_src_sanitizer() throws PolicyException{
//        String content = "<p><img alt=\"\" class=\"enlargeable\" src=\"nxfile/default/internalPicture/ttc:images/2/file/chat.jpeg\" />ppp</p> \r \n\n  \r";
//        
//        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
//                "antisamy-urls-policy.xml");
//         Policy policy = Policy.getInstance(is);
//        
//        sanitizer.policy = policy;
//        content = sanitizer.sanitizeString(content, "euh");
//        System.out.println(content);
//        
//        Assert.assertNotEquals(content, StringUtils.EMPTY);
//        
//    }

}
