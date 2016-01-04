/**
 * Copyright (C) 2015 Agro-Know, Deutsches Forschungszentrum für Künstliche Intelligenz, iMinds,
 * Institut für Angewandte Informatik e. V. an der Universität Leipzig,
 * Istituto Superiore Mario Boella, Tilde, Vistatec, WRIPL (http://freme-project.eu)
 *
 * This source code is derived from the Spring MVC Logging Filter
 * (https://github.com/isrsal/spring-mvc-logger) by Israel Zalmanov.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.freme.broker.tools.loggingfilter;

import java.io.PrintWriter;

public class TeePrintWriter extends PrintWriter{

    PrintWriter branch;

    public TeePrintWriter(PrintWriter main, PrintWriter branch) {
        super(main, true);
        this.branch = branch;
    }

    public void write(char buf[], int off, int len) {
        super.write(buf, off, len);
        super.flush();
        branch.write(buf, off, len);
        branch.flush();
    }

    public void write(String s, int off, int len) {
        super.write(s, off, len);
        super.flush();
        branch.write(s, off, len);
        branch.flush();
    }

    public void write(int c) {
        super.write(c);
        super.flush();
        branch.write(c);
        branch.flush();
    }

    public void flush() {
        super.flush();
        branch.flush();
    }
}
