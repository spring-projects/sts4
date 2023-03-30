package org.test;

import org.springframework.stereotype.Component;
import org.test.supertypes.AbstractBeanWithSupertypes;
import org.test.supertypes.Interface1OfBeanWithSupertypes;
import org.test.supertypes.Interface2OfBeanWithSupertypes;

@Component
public class BeanWithSupertypes extends AbstractBeanWithSupertypes implements Interface1OfBeanWithSupertypes, Interface2OfBeanWithSupertypes {

}
