package lk.bci.ordermanagementapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.order")
public class OrderProperties {

    private int maxItemsPerOrder = 10;
    private BigDecimal taxRate = BigDecimal.ZERO;
}
