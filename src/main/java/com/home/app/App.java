package com.home.app;

import org.dmg.pmml.*;
import org.jpmml.evaluator.*;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.model.ImportFilter;
import org.jpmml.model.JAXBUtil;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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


        /***** now that we have inspected a model how would we evaluate a test instance ******/
        System.out.println("Mining function: " + evaluator.getMiningFunction());

        System.out.println("Input schema:");
        System.out.println("\t" + "Active fields: " + evaluator.getActiveFields());
        System.out.println("\t" + "Group fields: " + evaluator.getGroupFields());

        System.out.println("Output schema:");
        System.out.println("\t" + "Target fields: " + evaluator.getTargetFields());
        System.out.println("\t" + "Output fields: " + evaluator.getOutputFields());


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

        Random random = new Random();
        for (int x=0; x<1000; x++) {
            Map<FieldName, org.jpmml.evaluator.FieldValue> features = new LinkedHashMap<FieldName, FieldValue>();

            // will need to use the active field as a key for the feature
            for (FieldName activeField : activeFields) {
                // The raw (ie. user-supplied) value could be any Java primitive value
                double rand = random.nextDouble()*-10;
                double rand2 = random.nextDouble()*10000;
                Object rawValue = rand; //+rand2;

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

            if (targetValue instanceof Computable) {
                Computable computable = (Computable) targetValue;
                Object primitiveValue = computable.getResult();

                System.out.print("**** Predicted result is: ");
                System.out.println(primitiveValue);
            }
        }

    }
}
