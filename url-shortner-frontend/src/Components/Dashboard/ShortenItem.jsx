import React, { useEffect, useState } from "react";
import dayjs from "dayjs";
import CopyToClipboard from "react-copy-to-clipboard";
import {
  ExternalLink,
  Calendar,
  Copy,
  Check,
  BarChart2,
  MousePointerClick,
  ChevronDown,
  ChevronUp,
  Loader2,
} from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { useStoreContext } from "../../contextApi/ContextApi";
import api from "../../api/api";
import Graph from "./Graph";
import { Button } from "../ui/button";
import { cn } from "../../lib/utils";

const ShortenItem = ({ originalUrl, shortUrl, clickCount, createdDate }) => {
  const { token } = useStoreContext();
  const navigate = useNavigate();
  const [isCopied, setIsCopied] = useState(false);
  const [analyticToggle, setAnalyticToggle] = useState(false);
  const [loader, setLoader] = useState(false);
  const [selectedUrl, setSelectedUrl] = useState("");
  const [analyticsData, setAnalyticsData] = useState([]);

  const subDomain = import.meta.env.VITE_REACT_FRONT_END_URL.replace(/^https?:\/\//, "");
  const fullShortUrl = `${import.meta.env.VITE_REACT_FRONT_END_URL}/s/${shortUrl}`;

  const handleCopy = () => {
    setIsCopied(true);
    setTimeout(() => setIsCopied(false), 2000);
  };

  const analyticsHandler = () => {
    if (!analyticToggle) {
      setSelectedUrl(shortUrl);
    }
    setAnalyticToggle(!analyticToggle);
  };

  const fetchAnalytics = async () => {
    setLoader(true);
    try {
      const endDate = new Date().toISOString().split("T")[0] + "T23:59:59";
      const { data } = await api.get(
        `/api/urls/analytics/${selectedUrl}?startDate=2024-12-01T00:00:00&endDate=${endDate}`,
        {
          headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
            Authorization: "Bearer " + token,
          },
        }
      );
      setAnalyticsData(data);
      setSelectedUrl("");
    } catch (error) {
      navigate("/error");
    } finally {
      setLoader(false);
    }
  };

  useEffect(() => {
    if (selectedUrl) fetchAnalytics();
  }, [selectedUrl]);

  return (
    <div
      className={cn(
        "glass rounded-2xl border border-white/8 overflow-hidden transition-all duration-300",
        "hover:border-white/15 group"
      )}
    >
      {/* Main Row */}
      <div className="p-5 flex flex-col sm:flex-row gap-4 justify-between">
        {/* URL Info */}
        <div className="flex-1 min-w-0 space-y-2">
          {/* Short URL */}
          <div className="flex items-center gap-2">
            <Link
              to={fullShortUrl}
              target="_blank"
              className="font-mono-url font-semibold text-violet-400 hover:text-violet-300 transition-colors truncate"
            >
              {subDomain}/s/{shortUrl}
            </Link>
            <ExternalLink className="w-3.5 h-3.5 text-violet-400/60 flex-shrink-0" />
          </div>

          {/* Original URL */}
          <p className="text-white/40 text-sm truncate max-w-md" title={originalUrl}>
            {originalUrl}
          </p>

          {/* Meta */}
          <div className="flex flex-wrap items-center gap-4 pt-1">
            <div className="flex items-center gap-1.5 text-emerald-400">
              <MousePointerClick className="w-4 h-4" />
              <span className="text-sm font-semibold">{clickCount}</span>
              <span className="text-sm text-white/30">
                {clickCount === 1 ? "click" : "clicks"}
              </span>
            </div>
            <div className="flex items-center gap-1.5 text-white/30">
              <Calendar className="w-3.5 h-3.5" />
              <span className="text-sm">{dayjs(createdDate).format("MMM DD, YYYY")}</span>
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="flex items-center gap-2 sm:flex-col sm:items-end justify-between sm:justify-center">
          <div className="flex gap-2">
            <CopyToClipboard text={fullShortUrl} onCopy={handleCopy}>
              <Button
                size="sm"
                variant={isCopied ? "secondary" : "outline"}
                className={cn(
                  "transition-all duration-300",
                  isCopied && "border-emerald-500/30 text-emerald-400"
                )}
              >
                {isCopied ? (
                  <>
                    <Check className="w-3.5 h-3.5" />
                    Copied
                  </>
                ) : (
                  <>
                    <Copy className="w-3.5 h-3.5" />
                    Copy
                  </>
                )}
              </Button>
            </CopyToClipboard>

            <Button
              size="sm"
              variant={analyticToggle ? "default" : "outline"}
              onClick={analyticsHandler}
              className={cn(
                analyticToggle && "bg-violet-600 border-violet-500"
              )}
            >
              <BarChart2 className="w-3.5 h-3.5" />
              Analytics
              {analyticToggle ? (
                <ChevronUp className="w-3.5 h-3.5" />
              ) : (
                <ChevronDown className="w-3.5 h-3.5" />
              )}
            </Button>
          </div>
        </div>
      </div>

      {/* Analytics Panel */}
      <div
        className={cn(
          "transition-all duration-300 overflow-hidden border-t border-white/8",
          analyticToggle ? "max-h-80" : "max-h-0 border-t-0"
        )}
      >
        <div className="p-5 h-72 relative">
          {loader ? (
            <div className="flex flex-col items-center justify-center h-full gap-3">
              <Loader2 className="w-8 h-8 text-violet-400 animate-spin" />
              <p className="text-white/30 text-sm">Loading analytics...</p>
            </div>
          ) : (
            <>
              {analyticsData.length === 0 && (
                <div className="absolute inset-0 flex flex-col items-center justify-center">
                  <BarChart2 className="w-10 h-10 text-white/10 mb-3" />
                  <p className="text-white/40 font-medium">No data for this period</p>
                  <p className="text-white/20 text-sm mt-1">
                    Share this link to see analytics
                  </p>
                </div>
              )}
              <Graph graphData={analyticsData} />
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default ShortenItem;