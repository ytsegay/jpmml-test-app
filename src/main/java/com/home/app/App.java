package com.home.app;

import org.dmg.pmml.*;
import org.jpmml.evaluator.*;
;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        /***********************
         *  Load a model example
         *************************/
        InputStream ins = App.class.getResourceAsStream("/pmml/sample.xml");
        Source source = ImportFilter.apply(new InputSource(ins));
        PMML pmml = JAXBUtil.unmarshalPMML(source);

        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
        ModelEvaluator<?> evaluator = modelEvaluatorFactory.newModelManager(pmml);

        /* get the active features and print name and what type they are */
        List<FieldName> activeFields = evaluator.getActiveFields();

        for(FieldName activeField : activeFields){
            MiningField miningField = evaluator.getMiningField(activeField);
            DataField dataField = evaluator.getDataField(activeField);

            DataType dataType = dataField.getDataType();

            OpType opType = dataField.getOpType();
            System.out.print(activeField.toString() + " type: ");
            switch(opType){
                case CONTINUOUS:

                    System.out.println("CONTINUOUS");
                    break;
                case CATEGORICAL:
                    System.out.println("CATEGORICAL");
                    break;
                case ORDINAL:
                    System.out.println("ORDINAL");
                    break;
                default:
                    System.out.println("NONE");
                    break;
            }
        }

        evaluator.verify();


        /***** now that we have inspected a model how would we evaluate a test instance ******/

        Map<FieldName, org.jpmml.evaluator.FieldValue> features = new LinkedHashMap<FieldName, FieldValue>();

        // will need to use the active field as a key for the feature
        for(FieldName activeField : activeFields){
            // The raw (ie. user-supplied) value could be any Java primitive value
            Object rawValue = 100.0;

            // The raw value is passed through:
            // 1) outlier treatment,
            // 2) missing value treatment,
            // 3) invalid value treatment and
            // 4) type conversion
            org.jpmml.evaluator.FieldValue activeValue = evaluator.prepare(activeField, rawValue);
            features.put(activeField, activeValue);

            // TODO: how would we pass on categorical variables??
        }

        /* execute the model */
        Map<FieldName, ?> results = evaluator.evaluate(features);
        /* the results of the model execute is a map, so we have to find the key for the response variable
         * so we are going to query it
         */
        FieldName targetName = evaluator.getTargetField();

        /* get the object with the result response */
        Object targetValue = results.get(targetName);

        if(targetValue instanceof Computable){
            Computable computable = (Computable)targetValue;
            Object primitiveValue = computable.getResult();

            System.out.print( "**** Predicted result is: " );
            System.out.println( primitiveValue );
        }

    }
}
