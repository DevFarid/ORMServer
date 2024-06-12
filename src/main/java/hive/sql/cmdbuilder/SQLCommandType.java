package hive.sql.cmdbuilder;

import hive.sql.QueryBuilder;

import java.util.logging.Logger;

/**
 * Enumerates the different types of SQL commands that can be sent to the server.
 * Created by SixEyes on 06/03/2024.
 */
public enum SQLCommandType {
    SELECT(SelectBuilder.class),
    ALTER_TABLE(AlterBuilder.class);

    private final Class<? extends QueryBuilder> builder;
    private final Logger logger = Logger.getLogger(SQLCommandType.class.getName());

    private Class<?>[] parameterTypes;
    private Object[] initArgs;

    SQLCommandType(Class<? extends QueryBuilder> builderClass) {
        this.builder = builderClass;
    }

    public SQLCommandType parameter(Class<?>... params) {
        this.parameterTypes = params;
        return this;
    }

    public SQLCommandType init(Object... initArgs) {
        this.initArgs = initArgs;
        return this;
    }

    public <T extends QueryBuilder> T getBuilderInstance() {
        try {
            if(parameterTypes != null && initArgs != null) {
                return (T) this.builder.getDeclaredConstructor(parameterTypes).newInstance(initArgs);
            } else {
                return (T) this.builder.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            logger.severe("Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Gets the SQL representation of the command type.
     * @return The SQL representation of the command type.
     */
    public String getSQL() {
        return name().replace("_", " ");
    }
}
