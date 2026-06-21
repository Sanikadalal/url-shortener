import React from "react";
import { motion } from "framer-motion";
import { Link2, BarChart3, Shield, Zap, ArrowRight } from "lucide-react";
import { Button } from "./ui/button";
import { useNavigate } from "react-router-dom";

const features = [
  {
    icon: Link2,
    title: "Simple URL Shortening",
    description:
      "Experience the ease of creating short, memorable URLs in just a few clicks. Our intuitive interface ensures you can start shortening URLs without any hassle.",
    color: "from-violet-500 to-purple-600",
  },
  {
    icon: BarChart3,
    title: "Powerful Analytics",
    description:
      "Gain insights into your link performance with our comprehensive analytics dashboard. Track clicks, geographical data, and referral sources.",
    color: "from-blue-500 to-cyan-600",
  },
  {
    icon: Shield,
    title: "Enhanced Security",
    description:
      "All shortened URLs are protected with advanced encryption, ensuring your data remains safe and secure at all times.",
    color: "from-emerald-500 to-teal-600",
  },
  {
    icon: Zap,
    title: "Fast and Reliable",
    description:
      "Enjoy lightning-fast redirects and high uptime with our reliable infrastructure. Your shortened URLs will always be available and responsive.",
    color: "from-amber-500 to-orange-600",
  },
];

const containerVariants = {
  hidden: { opacity: 0 },
  visible: { opacity: 1, transition: { staggerChildren: 0.12 } },
};

const itemVariants = {
  hidden: { opacity: 0, y: 20 },
  visible: { opacity: 1, y: 0, transition: { duration: 0.5 } },
};

const AboutPage = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-surface relative">
      <div className="fixed inset-0 grid-bg opacity-40 pointer-events-none" />
      <div className="fixed top-0 left-1/2 -translate-x-1/2 w-[600px] h-[300px] bg-gradient-to-b from-violet-600/15 to-transparent rounded-full blur-3xl pointer-events-none" />

      <div className="relative max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 pt-28 pb-20">
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="mb-16"
        >
          <p className="text-sm font-semibold text-violet-400 uppercase tracking-widest mb-4">
            About Linkora
          </p>
          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-black text-white leading-tight mb-6">
            Built for the modern
            <br />
            <span className="text-gradient">web developer.</span>
          </h1>
          <p className="text-white/50 text-lg leading-relaxed max-w-2xl">
            Linkora simplifies URL shortening for efficient sharing. We built a platform that's
            fast, secure, and packed with the analytics tools you need to understand your audience
            and optimize your campaigns.
          </p>
        </motion.div>

        {/* Features grid */}
        <motion.div
          variants={containerVariants}
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true, margin: "-80px" }}
          className="grid sm:grid-cols-2 gap-6 mb-20"
        >
          {features.map((feature) => (
            <motion.div
              key={feature.title}
              variants={itemVariants}
              className="glass rounded-2xl p-6 border border-white/8 hover:border-white/15 transition-all duration-300 group"
            >
              <div
                className={`w-12 h-12 rounded-xl bg-gradient-to-br ${feature.color} flex items-center justify-center mb-4 shadow-lg group-hover:scale-110 transition-transform duration-300`}
              >
                <feature.icon className="w-6 h-6 text-white" />
              </div>
              <h2 className="text-lg font-bold text-white mb-2">{feature.title}</h2>
              <p className="text-white/50 text-sm leading-relaxed">{feature.description}</p>
            </motion.div>
          ))}
        </motion.div>

        {/* Mission */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.6 }}
          className="relative overflow-hidden rounded-3xl"
        >
          <div className="absolute inset-0 bg-gradient-to-br from-violet-600/20 to-fuchsia-600/10" />
          <div className="relative glass border border-violet-500/20 rounded-3xl p-10 sm:p-14">
            <h2 className="text-2xl sm:text-3xl font-black text-white mb-4">Our Mission</h2>
            <p className="text-white/50 leading-relaxed mb-8 max-w-2xl">
              We believe sharing links should be simple, fast, and insightful. Linkora was
              built to empower individuals and teams to share smarter — with visibility into
              how every link performs across the web.
            </p>
            <Button
              variant="gradient"
              onClick={() => navigate("/register")}
              className="shadow-lg shadow-violet-500/25"
            >
              Join Linkora
              <ArrowRight className="w-4 h-4 ml-1" />
            </Button>
          </div>
        </motion.div>
      </div>
    </div>
  );
};

export default AboutPage;