//
//  ReportCompareViewTableViewCell.swift
//  LightAIModelVerification
//
//  Created by leeint on 2018. 10. 26..
//  Copyright © 2018년 leeint. All rights reserved.
//

import UIKit
import Charts

class ReportCompareViewTableViewCell: UITableViewCell {

    
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
        
        let labels = ["MobileNet", "InceptionV3"]
        var dataEntries: [BarChartDataEntry] = []
        
        for i in 0...(SystemInfo.MODEL_NUM-1) {
            let values = SystemInfo.getPerformance(modelIdx: i)
            var total = 0.0
            for vote in values{
                total += Double(vote)
            }
            let votesTotal = Double(values.count)
            let average = (total/votesTotal)*1000
            let dataEntry = BarChartDataEntry(x: Double(i), y: Double(average))
            
            dataEntries.append(dataEntry)
        }
        
        let chartDataSet = BarChartDataSet(values: dataEntries, label: "Millisecond")
        chartDataSet.colors = [NSUIColor(red: 0, green: 102/255, blue: 166/255, alpha: 1), NSUIColor(red: 241/255, green: 89/255, blue: 34/255, alpha: 1)]
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
