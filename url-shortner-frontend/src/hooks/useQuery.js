import { useQuery } from "@tanstack/react-query";
import api from "../api/api"


export const useFetchMyShortUrls = (token) => {
    return useQuery({
        queryKey: ["my-shortenurls"],
        queryFn: async () => {
            return await api.get(
                "/api/urls/myurls",
                {
                    headers: {
                        "Content-Type": "application/json",
                        Accept: "application/json",
                        Authorization: "Bearer " + token,
                    },
                }
            );
        },
        select: (data) => {
            const sortedData = data.data.sort(
                (a, b) => new Date(b.createdDate) - new Date(a.createdDate)
            );
            return sortedData;
        },
        staleTime: 5000
    });
};

export const useFetchTotalClicks = (token) => {
    return useQuery({
        queryKey: ["url-totalclick"],
        queryFn: async () => {
            const endDate = new Date().toISOString().split("T")[0];
            return await api.get(
                `/api/urls/totalClicks?startDate=2024-01-01&endDate=${endDate}`,
                {
                    headers: {
                        "Content-Type": "application/json",
                        Accept: "application/json",
                        Authorization: "Bearer " + token,
                    },
                }
            );
        },
        select: (data) => {
            const convertToArray = Object.keys(data.data).map((key) => ({
                clickDate: key,
                count: data.data[key], // data.data[2024-01-01]
            }));
            return convertToArray;
        },
        staleTime: 5000
    });
};