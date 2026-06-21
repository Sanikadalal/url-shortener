import React, { useState } from "react";
import { motion } from "framer-motion";
import { useStoreContext } from "../../contextApi/ContextApi";
import { useFetchMyShortUrls, useFetchTotalClicks } from "../../hooks/useQuery";
import { useNavigate } from "react-router-dom";
import { Plus, Link2, TrendingUp } from "lucide-react";
import Graph from "./Graph";
import ShortenUrlList from "./ShortenUrlList";
import { Button } from "../ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from "../ui/dialog";
import CreateNewShorten from "./CreateNewShorten";
import Loader from "../Loader";

const DashboardLayout = () => {
  const { token } = useStoreContext();
  const navigate = useNavigate();
  const [shortenPopUp, setShortenPopUp] = useState(false);

  const {
    isLoading,
    isError: isShortUrlsError,
    data: myShortenUrls,
    refetch,
  } = useFetchMyShortUrls(token);

  const {
    isLoading: loader,
    isError: isTotalClicksError,
    data: totalClicks,
  } = useFetchTotalClicks(token);

  React.useEffect(() => {
    if (isShortUrlsError || isTotalClicksError) {
      navigate("/error");
    }
  }, [isShortUrlsError, isTotalClicksError, navigate]);

  if (loader) {
    return <Loader />;
  }

  const totalClickCount = (myShortenUrls || []).reduce(
    (acc, item) => acc + (item.clickCount || 0),
    0
  );

  return (
    <div className="min-h-screen bg-surface relative">
      <div className="fixed inset-0 grid-bg opacity-30 pointer-events-none" />

      <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-24 pb-16">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-10"
        >
          <div>
            <h1 className="text-3xl sm:text-4xl font-black text-white">Dashboard</h1>
            <p className="text-white/40 mt-1 text-sm">
              Manage and track your shortened links
            </p>
          </div>
          <Button
            variant="gradient"
            onClick={() => setShortenPopUp(true)}
            className="shadow-lg shadow-violet-500/25 self-start sm:self-auto"
          >
            <Plus className="w-4 h-4" />
            New Short Link
          </Button>
        </motion.div>

        {/* Stats row */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.1 }}
          className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8"
        >
          {[
            {
              label: "Total Links",
              value: (myShortenUrls || []).length,
              icon: Link2,
              color: "from-violet-500 to-purple-600",
            },
            {
              label: "Total Clicks",
              value: totalClickCount,
              icon: TrendingUp,
              color: "from-blue-500 to-cyan-600",
            },
            {
              label: "This Month",
              value: (totalClicks || []).reduce((a, b) => a + (b.count || 0), 0),
              icon: TrendingUp,
              color: "from-emerald-500 to-teal-600",
            },
            {
              label: "Avg Clicks/Link",
              value:
                (myShortenUrls || []).length > 0
                  ? Math.round(totalClickCount / (myShortenUrls || []).length)
                  : 0,
              icon: TrendingUp,
              color: "from-amber-500 to-orange-600",
            },
          ].map((stat) => (
            <div
              key={stat.label}
              className="glass rounded-2xl p-5 border border-white/8 hover:border-white/15 transition-all duration-300"
            >
              <div className="flex items-center justify-between mb-3">
                <span className="text-white/40 text-xs font-semibold uppercase tracking-wider">
                  {stat.label}
                </span>
                <div
                  className={`w-8 h-8 rounded-lg bg-gradient-to-br ${stat.color} flex items-center justify-center`}
                >
                  <stat.icon className="w-4 h-4 text-white" />
                </div>
              </div>
              <div className="text-3xl font-black text-white">{stat.value}</div>
            </div>
          ))}
        </motion.div>

        {/* Analytics Chart */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="glass rounded-2xl border border-white/8 p-6 mb-8"
        >
          <div className="flex items-center justify-between mb-5">
            <h2 className="text-lg font-bold text-white">Click Analytics</h2>
            <span className="text-xs text-white/30 glass px-3 py-1 rounded-full border border-white/8">
              Last 30 days
            </span>
          </div>
          <div className="h-64 relative">
            {(totalClicks || []).length === 0 && (
              <div className="absolute inset-0 flex flex-col justify-center items-center">
                <TrendingUp className="w-12 h-12 text-white/10 mb-3" />
                <h3 className="text-white/50 font-semibold">No data yet</h3>
                <p className="text-white/30 text-sm mt-1">
                  Share your links to see analytics
                </p>
              </div>
            )}
            <Graph graphData={totalClicks || []} />
          </div>
        </motion.div>

        {/* Links List */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.3 }}
        >
          <div className="flex items-center justify-between mb-5">
            <h2 className="text-lg font-bold text-white">Your Links</h2>
            {(myShortenUrls || []).length > 0 && (
              <span className="text-xs text-white/40">
                {(myShortenUrls || []).length} links total
              </span>
            )}
          </div>

          {!isLoading && (myShortenUrls || []).length === 0 ? (
            <div className="glass rounded-2xl border border-white/8 border-dashed p-16 flex flex-col items-center justify-center text-center">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-violet-500/20 to-fuchsia-600/20 flex items-center justify-center mb-4 border border-violet-500/20">
                <Link2 className="w-8 h-8 text-violet-400" />
              </div>
              <h3 className="text-white font-bold text-lg mb-2">No links yet</h3>
              <p className="text-white/40 text-sm mb-6 max-w-xs">
                Create your first shortened link and start tracking its performance.
              </p>
              <Button
                variant="gradient"
                onClick={() => setShortenPopUp(true)}
                className="shadow-lg shadow-violet-500/25"
              >
                <Plus className="w-4 h-4" />
                Create First Link
              </Button>
            </div>
          ) : (
            <ShortenUrlList data={myShortenUrls || []} />
          )}
        </motion.div>
      </div>

      {/* Dialog */}
      <Dialog open={shortenPopUp} onOpenChange={setShortenPopUp}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>Create Short URL</DialogTitle>
            <DialogDescription>
              Paste your long URL below and we'll generate a short, trackable link.
            </DialogDescription>
          </DialogHeader>
          <CreateNewShorten setOpen={setShortenPopUp} refetch={refetch} />
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default DashboardLayout;