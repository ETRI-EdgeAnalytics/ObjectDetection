//
//  ResultViewController.swift
//  LightAIModelVerification
//
//  Created by leeint on 2018. 10. 3..
//  Copyright © 2018년 leeint. All rights reserved.
//

import UIKit
import Vision
import CoreMedia


class ResultViewController: UIViewController {

    @IBOutlet weak var performanceLabel: UILabel!
    @IBOutlet weak var resultLabel: UILabel!
    @IBOutlet weak var resultLabel2: UILabel!
    @IBOutlet weak var resultLabel3: UILabel!
    @IBOutlet weak var resultLabel4: UILabel!
    @IBOutlet weak var resultLabel5: UILabel!
    
    
    @IBOutlet weak var captureImageView: UIImageView!
    var captureImage:UIImage!
    
    typealias Prediction = (String, Double)
    
    var results: [Prediction]?
    var performance : Double!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        //resultLabel.numberOfLines = 0
        
        // Do any additional setup after loading the view.
    }

    override func viewWillAppear(_ animated: Bool) {
      
        super.viewWillAppear(animated) // No need for semicolon
        captureImageView.image = captureImage
        show()
    }

    
    func show() {
        
        for (i, pred) in (results?.enumerated())! {
            
            var fullString = pred.0//.components(separatedBy: " ")
            //let prefix:Int = fullString[0].count
            //let idx = pred.0.index(pred.0.startIndex, offsetBy: prefix)
            //let className:String = String(pred.0[idx...])
            //let className:String = fullString
            
            switch(i) {
            case 0:
                resultLabel.text = String(format: "%d: %@ (%3.2f%%)", i + 1, fullString, pred.1 * 100)
                break;
            case 1:
                resultLabel2.text = String(format: "%d: %@ (%3.2f%%)", i + 1, fullString, pred.1 * 100)
                break;
            case 2:
                resultLabel3.text = String(format: "%d: %@ (%3.2f%%)", i + 1, fullString, pred.1 * 100)
                break;
            case 3:
                resultLabel4.text = String(format: "%d: %@ (%3.2f%%)", i + 1, fullString, pred.1 * 100)
                break;
            case 4:
                resultLabel5.text = String(format: "%d: %@ (%3.2f%%)", i + 1, fullString, pred.1 * 100)
                break;
            default:
                break;
            }
            
        }
        //resultLabel.text = s.joined(separator: "\n")
        
        //let latency = CACurrentMediaTime() - startTimes.remove(at: 0)
        //let fps = self.measureFPS()
        performanceLabel.text = String(format: "Performance %f s", performance!)
        //resultLabel.sizeToFit()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    

    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
    }
    */

}
