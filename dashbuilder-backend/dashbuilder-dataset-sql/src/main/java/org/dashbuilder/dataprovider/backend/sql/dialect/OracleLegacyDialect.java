/**
 * Copyright (C) 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dashbuilder.dataprovider.backend.sql.dialect;

import org.dashbuilder.dataprovider.backend.sql.model.Select;

/**
 * Oracle dialect for database versions previous to the 12c release
 */
public class OracleLegacyDialect extends OracleDialect {

    @Override
    public String[] getExcludedColumns() {
        return new String[] {"RN"};
    }

    @Override
    public String getSQL(Select select) {

        String sql = super.getSQL(select);

        int offset = select.getOffset();
        int limit = select.getLimit();
        if (limit <= 0 && offset <= 0) {
            return sql;
        }

        String result = "SELECT * FROM (SELECT Q.*, ROWNUM RN FROM (" + sql + ") Q) WHERE ";
        if (offset > 0 && limit > 0) {
            result += "RN > " + offset + " AND RN <= "  + (offset + limit);
        }
        else if (offset > 0) {
            result += "RN >= " + offset;
        }
        else if (limit > 0) {
            result += "RN <= " + limit;
        }
        return result;
    }
}
