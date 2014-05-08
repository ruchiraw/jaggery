package org.jaggeryjs.jaggery.core.javax.test;

import junit.framework.TestCase;
import org.jaggeryjs.core.JaggeryEngine;
import org.jaggeryjs.core.JaggeryException;
import org.jaggeryjs.core.JaggeryReaderImpl;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import java.io.InputStreamReader;

public class JaggeryContextTest extends TestCase {

    public void test() throws JaggeryException {

        long t1 = System.currentTimeMillis();
        final JaggeryEngine e1 = new JaggeryEngine("webapp",
                new InputStreamReader(this.getClass().getResourceAsStream("/test1.js")),
                new JaggeryReaderImpl());
        Bindings init = new SimpleBindings();
        init.put("request", "req");
        init.put("response", "res");
        e1.exec(init);

        long t2 = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {

            //new Thread(new Runnable() {
                //@Override
               // public void run() {
                    Bindings bindings = new SimpleBindings();
                    bindings.put("request", "req");
                    bindings.put("response", "res");
                    try {
                        e1.exec(bindings);
                    } catch (JaggeryException e) {
                        e.printStackTrace();
                    }
               // }
           // }).start();
        }


        System.out.println(System.currentTimeMillis() - t2);
        System.out.println(System.currentTimeMillis() - t1);

        //assertEquals(10.0, scope.get("x"));
    }
}



