//
//  ViewController.swift
//  LightAIModelVerification
//
//  Created by leeint on 2018. 10. 3..
//  Copyright © 2018년 leeint. All rights reserved.
//

import UIKit
import AVFoundation
import Photos
import Vision
import CoreMedia


class ViewController: UIViewController, UIImagePickerControllerDelegate, UINavigationControllerDelegate, UIPickerViewDataSource, UIPickerViewDelegate {
    

    var request: VNCoreMLRequest!
    var startTimes: CFTimeInterval!
    var captureImage:UIImage?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        

        checkPermission()
        setUpVision()
        
        // Do any additional setup after loading the view, typically from a nib.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    func checkPermission() {
        let photoAuthorizationStatus = PHPhotoLibrary.authorizationStatus()
        switch photoAuthorizationStatus {
        case .authorized:
            print("Access is granted by user")
        case .notDetermined:
            PHPhotoLibrary.requestAuthorization({
                (newStatus) in
                print("status is \(newStatus)")
                if newStatus ==  PHAuthorizationStatus.authorized {
                    /* do stuff here */
                    print("success")
                }
            })
            print("It is not determined until now")
        case .restricted:
            // same same
            print("User do not have access to photo album.")
        case .denied:
            // same same
            print("User has denied the permission.")
        }
    }
        
    func setUpVision() {

        //선택된 모델 정보 획득
        let modelIdx = SystemInfo.getModel()
        
        //MobileNet일 경우
        if(modelIdx == 0) {
            //MobileNet 모델 초기화
            let model =  MobileNet()
            //CoreML model 파일 생성
            guard let visionModel = try? VNCoreMLModel(for: model.model) else {
                print("Error: could not create Vision model")
                return
            }
            //CoreML Request 생성, Predict결과는 requestDidComplete handler호출
            request = VNCoreMLRequest(model: visionModel, completionHandler: requestDidComplete)
        //InceptionV3인 경우
        } else {
            //InceptinoV3 모델 초기화
            let model = Inceptionv3()
            //CoreML model 파일 생성
            guard let visionModel = try? VNCoreMLModel(for: model.model) else {
                print("Error: could not create Vision model")
                return
            }
            //CoreML Request 생성, Predict결과는 requestDidComplete handler호출
            request = VNCoreMLRequest(model: visionModel, completionHandler: requestDidComplete)
        }
        
        request.imageCropAndScaleOption = .centerCrop
    }

    func predict(imge: CGImage) {
        //시간 측정을 시작한다.
        startTimes = CACurrentMediaTime()
        //카메라, 갤러리에서 들어온 이미지를 이용하여 Handler를 생성한다.
        let handler = VNImageRequestHandler(cgImage: imge)
        //이미지 Predict를 수행한다.
        try? handler.perform([request])
    }

    
    func requestDidComplete(request: VNRequest, error: Error?) {
        //수행한 결과를 받는다.
        if let observations = request.results as? [VNClassificationObservation] {
            
            //최상위 5개를 뽑아낸다.
            let top5 = observations.prefix(through: 4)
                .map { ($0.identifier, Double($0.confidence)) }
            
            //Result view controller에 징보를 전달한다.
            if let viewController = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "ResultViewController") as? ResultViewController {
                if let navigator = navigationController {
                    
                    let modelIdx = SystemInfo.getModel()
                    let timeDiff:Double = CACurrentMediaTime() - startTimes
                    SystemInfo.addPerformance(time: timeDiff, modelIdx: modelIdx)
                    
                    viewController.results = top5
                    viewController.performance = timeDiff
                    viewController.captureImage = captureImage

                    navigator.pushViewController(viewController, animated: true)
                }
            }
        }
    }


    @IBAction func modelSelectPressed(_ sender: Any) {
        
        let vc = UIViewController()
        vc.preferredContentSize = CGSize(width: 250,height: 200)
        let pickerView = UIPickerView(frame: CGRect(x: 0, y: 0, width: 250, height: 200))
        pickerView.delegate = self
        pickerView.dataSource = self
        pickerView.selectRow(SystemInfo.getModel(), inComponent: 0, animated: false)
        vc.view.addSubview(pickerView)
        let editRadiusAlert = UIAlertController(title: "모델선택", message: "", preferredStyle: UIAlertControllerStyle.alert)
        editRadiusAlert.setValue(vc, forKey: "contentViewController")
        editRadiusAlert.addAction(UIAlertAction(title: "확인", style: .default, handler: { (action: UIAlertAction!) in
            self.setUpVision()
        }))
        editRadiusAlert.addAction(UIAlertAction(title: "취소", style: .cancel, handler: nil))
        self.present(editRadiusAlert, animated: true)
        
    }
    
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return 2
    }
    
    //MARK: Delegate
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        
        if(row == 0) {
            return "MobileNet"
        }
        return "InceptionV3"
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        SystemInfo.setModel(modelIdx: row)
    }

    @IBAction func cameraPressed(_ sender: Any) {
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.camera) {
            let imagePicker = UIImagePickerController()
            imagePicker.delegate = self
            imagePicker.sourceType = UIImagePickerControllerSourceType.camera
            imagePicker.allowsEditing = false
            self.present(imagePicker, animated: true, completion: nil)
        }
        else
        {
            let alert  = UIAlertController(title: "Warning", message: "You don't have camera", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
            self.present(alert, animated: true, completion: nil)
        }

    }
    
    @IBAction func galleryPressed(_ sender: Any) {
        if UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.photoLibrary){
            let imagePicker = UIImagePickerController()
            imagePicker.delegate = self
            imagePicker.allowsEditing = false
            imagePicker.sourceType = UIImagePickerControllerSourceType.photoLibrary
            self.present(imagePicker, animated: true, completion: nil)
        }
        else
        {
            let alert  = UIAlertController(title: "Warning", message: "You don't have perission to access gallery.", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
            self.present(alert, animated: true, completion: nil)
        }

    }
    
    @objc func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [String : AnyObject]) {
        
        if let pickedImage = info[UIImagePickerControllerOriginalImage] as? UIImage {
            // imageViewPic.contentMode = .scaleToFill
            captureImage = pickedImage
            predict(imge: pickedImage.cgImage!)
        }
        picker.dismiss(animated: true, completion: nil)
        
    }
}

