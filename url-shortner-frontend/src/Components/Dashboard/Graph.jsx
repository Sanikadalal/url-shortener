import React from "react";
import { Bar } from "react-chartjs-2";
import {
  Chart as ChartJS,
  BarElement,
  CategoryScale,
  LinearScale,
  Legend,
  Tooltip,
  Filler,
} from "chart.js";

ChartJS.register(BarElement, Tooltip, CategoryScale, LinearScale, Legend, Filler);

const Graph = ({ graphData }) => {
  const labels = graphData?.map((item) => item.clickDate);
  const userPerDay = graphData?.map((item) => item.count);

  const data = {
    labels:
      graphData.length > 0
        ? labels
        : ["", "", "", "", "", "", "", "", "", "", "", "", "", ""],
    datasets: [
      {
        label: "Clicks",
        data:
          graphData.length > 0
            ? userPerDay
            : [1, 2, 3, 4, 5, 6, 7, 6, 5, 4, 3, 2, 1],
        backgroundColor:
          graphData.length > 0
            ? "rgba(139, 92, 246, 0.7)"
            : "rgba(139, 92, 246, 0.08)",
        borderColor:
          graphData.length > 0
            ? "rgba(139, 92, 246, 1)"
            : "rgba(139, 92, 246, 0.1)",
        borderWidth: 1,
        borderRadius: 6,
        borderSkipped: false,
        barThickness: 16,
      },
    ],
  };

  const options = {
    maintainAspectRatio: false,
    responsive: true,
    plugins: {
      legend: {
        display: false,
      },
      tooltip: {
        backgroundColor: "rgba(15, 15, 26, 0.95)",
        borderColor: "rgba(139, 92, 246, 0.3)",
        borderWidth: 1,
        titleColor: "rgba(255,255,255,0.9)",
        bodyColor: "rgba(255,255,255,0.6)",
        padding: 10,
        cornerRadius: 8,
      },
    },
    scales: {
      y: {
        beginAtZero: true,
        grid: {
          color: "rgba(255,255,255,0.05)",
        },
        border: {
          display: false,
        },
        ticks: {
          color: "rgba(255,255,255,0.3)",
          font: { size: 11, family: "Inter" },
          callback: function (value) {
            if (Number.isInteger(value)) return value;
            return "";
          },
        },
      },
      x: {
        grid: {
          display: false,
        },
        border: {
          display: false,
        },
        ticks: {
          color: "rgba(255,255,255,0.3)",
          font: { size: 11, family: "Inter" },
          maxRotation: 45,
        },
      },
    },
  };

  return <Bar className="w-full" data={data} options={options} />;
};

export default Graph;