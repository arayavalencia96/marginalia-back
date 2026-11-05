package com.marginalia.api.service;

import com.marginalia.api.domain.ContentBlockType;
import com.marginalia.api.exception.InvalidContentBlockException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StructuredBlockContentTest {
    @Test
    void acceptsBlankCellsInRectangularTables() {
        assertThat(StructuredBlockContent.parse(ContentBlockType.TABLE, "[[\"\",\"value\"],[\"a\",\"b\"]]").size())
                .isEqualTo(2);
    }

    @Test
    void rejectsRaggedTablesAndNonTextCells() {
        for (String content : new String[]{"[[\"a\"],[\"b\",\"c\"]]", "[[12]]", "[]", "null"}) {
            assertThatThrownBy(() -> StructuredBlockContent.parse(ContentBlockType.TABLE, content))
                    .isInstanceOf(InvalidContentBlockException.class);
        }
    }

    @Test
    void rejectsTasksWithoutTextOrBooleanState() {
        for (String content : new String[]{"[{\"text\":\"\",\"checked\":false}]", "[{\"text\":\"Read\"}]"}) {
            assertThatThrownBy(() -> StructuredBlockContent.parse(ContentBlockType.TODO_LIST, content))
                    .isInstanceOf(InvalidContentBlockException.class);
        }
    }
}
