package org.pdxfinder.services.graphqlclient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class GraphQlBuilder {

    private GraphQlBuilder() {

    }

    public static String selectQuery(Select selectQuery){

        String space = " ";
        String table = selectQuery.getTable();

        String columns = selectQuery.getColumns().stream()
                .map(Object::toString)
                .collect(Collectors.joining(space));

        Optional<Map<String, Condition>> conditions = Optional.ofNullable(selectQuery.getConditions());
        if (conditions.isPresent()){
            table = String.format("%s(%s)", table, buildCondition(conditions.get()));
        }

        String query =  String.format("%s{ %s }", table, columns);
        return String.format("{ \"query\":\"query MyQuery { %s }\" }", query);
    }


    private static String buildCondition(Map<String, Condition> conditions){

        AtomicReference<String> where = new AtomicReference<>("");
        conditions.forEach((key,condition)->{

            String clause = "";
            List<String> valueList = condition.getValue();
            String values = valueList.stream()
                    .map(data -> String.format("\\\"%s\\\"", data))
                    .collect(Collectors.joining(", ", "[", "]"));

            clause = String.format("where: { %s : {%s %s }}", key, condition.getOperator().get(), values);
            where.set(clause);
        });

        return where.get();
    }

}
