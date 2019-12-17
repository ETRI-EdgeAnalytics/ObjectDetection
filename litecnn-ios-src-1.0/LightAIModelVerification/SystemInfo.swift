//
//  SystemInfo.swift
//  LightAIModelVerification
//
//  Created by leeint on 2018. 10. 26..
//  Copyright © 2018년 leeint. All rights reserved.
//

import Foundation

class SystemInfo {

    public static let MODEL_NUM:Int = 2
    
    public static func setModel(modelIdx:Int) {
        let defaults = UserDefaults.standard
        defaults.set(modelIdx, forKey: "model_index")
    }
    
    public static func getModel() -> Int {
        let defaults = UserDefaults.standard
        let value:Int? = defaults.integer(forKey: "model_index")
        
        if(value == nil) {
            return 0
        }
        return value!
    }

    
    public static func addPerformance(time:Double, modelIdx:Int){

        let defaults = UserDefaults.standard
        var array:[Double]? = defaults.array(forKey: String(format: "model_history_%d", modelIdx))  as? [Double] ?? [Double]()

        array?.insert(time, at: 0)
        if(array!.count > 20) {
            array?.remove(at: 20)
        }
        
        defaults.set(array, forKey: String(format: "model_history_%d", modelIdx))

    }

    public static func getPerformance(modelIdx:Int) -> [Double]{
        
        let defaults = UserDefaults.standard
        let array:[Double]? = defaults.array(forKey: String(format: "model_history_%d", modelIdx))  as? [Double] ?? [Double]()
        
        return array!
    }

}

