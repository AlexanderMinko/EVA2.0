package com.english.eva.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.english.eva.domain.Example;
import org.jooq.DSLContext;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class ExampleRepository {

    private final DSLContext dsl;

    public ExampleRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Example save(Example example) {
        if (example.getId() == null) {
            var record = dsl.insertInto(table("example"))
                    .columns(field("meaning_id"), field("text"))
                    .values(example.getMeaningId(), example.getText())
                    .returningResult(field("id"))
                    .fetchOne();
            example.setId(record.get(0, Long.class));
        } else {
            dsl.update(table("example"))
                    .set(field("text"), example.getText())
                    .where(field("id").eq(example.getId()))
                    .execute();
        }
        return example;
    }

    public List<Example> findByMeaningId(Long meaningId) {
        return dsl.selectFrom(table("example"))
                .where(field("meaning_id").eq(meaningId))
                .fetch(r -> new Example(
                        r.get("ID", Long.class),
                        r.get("MEANING_ID", Long.class),
                        r.get("TEXT", String.class)
                ));
    }

    public Map<Long, List<Example>> findByMeaningIds(Set<Long> meaningIds) {
        if (meaningIds.isEmpty()) return Map.of();
        var result = new HashMap<Long, List<Example>>();
        dsl.selectFrom(table("example"))
                .where(field("meaning_id").in(meaningIds))
                .fetch(r -> {
                    var example = new Example(
                            r.get("ID", Long.class),
                            r.get("MEANING_ID", Long.class),
                            r.get("TEXT", String.class));
                    result.computeIfAbsent(example.getMeaningId(), k -> new ArrayList<>()).add(example);
                    return example;
                });
        return result;
    }

    public void deleteByMeaningId(Long meaningId) {
        dsl.deleteFrom(table("example"))
                .where(field("meaning_id").eq(meaningId))
                .execute();
    }

    public void deleteByMeaningIds(List<Long> meaningIds) {
        if (meaningIds.isEmpty()) return;
        dsl.deleteFrom(table("example"))
                .where(field("meaning_id").in(meaningIds))
                .execute();
    }
}
