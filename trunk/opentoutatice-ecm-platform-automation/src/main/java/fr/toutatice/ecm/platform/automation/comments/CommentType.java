package fr.toutatice.ecm.platform.automation.comments;

import org.apache.commons.lang.StringUtils;

/**
 * Comment types enumeration.
 * 
 * @author Cédric Krommenhoek
 */
public enum CommentType {

    /** Default comment type. */
    COMMENT("Comment", null, "comment"),
    /** Forum thread post comment type. */
    POST("Post", "Thread", "post");


    /** Default value. */
    private static final CommentType DEFAULT = COMMENT;


    /** Comment type. */
    private final String type;
    /** Parent document type. */
    private final String parentType;
    /** Comment schema. */
    private final String schema;


    /**
     * Constructor.
     * 
     * @param type comment type
     * @param parentType parent document type
     * @param schema comment schema
     */
    private CommentType(String type, String parentType, String schema) {
        this.type = type;
        this.parentType = parentType;
        this.schema = schema;
    }


    /**
     * Get comment type from parent document type.
     * 
     * @param parentType parent document type
     * @return comment type
     */
    public static CommentType fromParentType(String parentType) {
        CommentType result = DEFAULT;

        for (CommentType value : CommentType.values()) {
            if (StringUtils.equals(value.parentType, parentType)) {
                result = value;
                break;
            }
        }

        return result;
    }


    /**
     * Getter for type.
     * 
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Getter for parentType.
     * 
     * @return the parentType
     */
    public String getParentType() {
        return parentType;
    }

    /**
     * Getter for schema.
     * 
     * @return the schema
     */
    public String getSchema() {
        return schema;
    }

}
