package in.truethics.ethics.ethicsapiv10.common;

import org.springframework.stereotype.Component;

import java.text.DecimalFormat;

@Component
public class NumFormat {
    public Double numFormat(Double input) {
        DecimalFormat numberFormat = new DecimalFormat("#.00");
        Double result = Double.parseDouble(numberFormat.format(input));
        return result;
    }
}
