/*
 * Copyright to the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package calculator;

import org.rioproject.cybernode.StaticCybernode;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runner.RunWith;
import java.io.File;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Collection;
import java.util.Arrays;

/**
 * Example testing the Calculator service and it's required services from the
 * <tt>OperationalString</tt>
 */
@RunWith(Parameterized.class)
public class ITCalculatorTest {
    String opstring;
    Calculator calculator;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        String opstring = System.getProperty("opstring");
        Assert.assertNotNull("no opstring given", opstring);
        return Arrays.asList(new Object[][] {{ opstring }});
    }

    public ITCalculatorTest(String opstring) {
        this.opstring = opstring;
    }

    @Before
    public void setupCalculator() throws Exception {
        StaticCybernode cybernode = new StaticCybernode();
        Map<String, Object> map = cybernode.activate(new File(opstring));
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String beanName = entry.getKey();
            Object beanImpl = entry.getValue();
            if (beanName.equals("Calculator"))
                calculator = (Calculator) beanImpl;
        }
    }

    @Test
    public void testCalculator() throws RemoteException {
        Assert.assertNotNull(calculator);
        add();
        subtract();
        divide();
        multiply();
    }

    void add() throws RemoteException {
        double val = calculator.add(3, 2);
        assertEquals(val, 5, 0);
        System.out.println("    3 + 2 = " + val);
    }

    void subtract() throws RemoteException {
        double val = calculator.subtract(3, 2);
        assertEquals(val, 1, 0);
        System.out.println("    3 - 2 = " + val);
    }

    void divide() throws RemoteException {
        double val = calculator.divide(10, 10);
        assertEquals(val, 1, 0);
        System.out.println("    10 % 10 = " + val);
    }

    void multiply() throws RemoteException {
        double val = calculator.multiply(10, 10);
        assertEquals(val, 100, 0);
        System.out.println("    10 * 10 = " + val);
    }

}
