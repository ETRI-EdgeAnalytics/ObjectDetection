//
//  ReportCompareViewTableViewCell.swift
//  LightAIModelVerification
//
//  Created by leeint on 2018. 10. 26..
//  Copyright © 2018년 leeint. All rights reserved.
//

import UIKit
import Charts

class ReportInceptionViewTableViewCell: UITableViewCell {

    
    @IBOutlet weak var infoLabel: UILabel!
    @IBOutlet weak var compareChart: BarChartView!
    
    override func awakeFromNib() {
        super.awakeFromNib()
        //initChart()
        // Initialization code
    }

    override func setSelected(_ selected: Bool, animated: Bool) {
        super.setSelected(selected, animated: animated)

        // Configure the view for the selected state
    }

    func initChart() {
        
        var labels:[String] = [String]()
        var dataEntries: [BarChartDataEntry] = []
        let values = SystemInfo.getPerformance(modelIdx: 1)
        var min:Double = 100000.0
        var max:Double = 0.0
        var total:Double = 0.0
        
        for (index, val) in values.enumerated() {
            
            if(val < min) {
                min = val
            }
            
            if(val > max) {
                max = val
            }
            total = total+val
            let dataEntry = BarChartDataEntry(x: Double(index), y: Double(val))
            labels.append(String(format: "%d", index))
            dataEntries.append(dataEntry)
        }
        
        infoLabel.text = String(format: "Min:%.3f Max:%.3f Avg:%.3f", min, max, total/Double(values.count))
        
        let chartDataSet = BarChartDataSet(values: dataEntries, label: "Millisecond")
        chartDataSet.colors = [NSUIColor(red: 0, green: 102/255, blue: 166/255, alpha: 1)]
        let chartData = BarChartData(dataSet: chartDataSet)
        chartData.barWidth = 0.5
        compareChart.leftAxis.axisMinimum = 0
        compareChart.xAxis.valueFormatter = IndexAxisValueFormatter(values:labels)
        compareChart.xAxis.labelPosition = .bottom
        //compareChart.xAxis.axisMinimum = 0.0
        compareChart.xAxis.granularityEnabled = true
        compareChart.xAxis.drawGridLinesEnabled = false
        compareChart.xAxis.drawAxisLineEnabled = false
        compareChart.rightAxis.drawGridLinesEnabled = false
        compareChart.rightAxis.drawAxisLineEnabled = false
        compareChart.chartDescription?.text = ""
        compareChart.data = chartData
    }
}
