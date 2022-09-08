package com.chl.victory.wmall;

import com.chl.victory.WMallApplication;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WMallApplication.class, properties = {"spring.profiles.active=dev"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BaseTest {

}
