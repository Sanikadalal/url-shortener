import React, { useState } from "react";
import { useStoreContext } from "../../contextApi/ContextApi";
import { useForm } from "react-hook-form";
import { Input } from "../ui/input";
import { Button } from "../ui/button";
import api from "../../api/api";
import toast from "react-hot-toast";
import { Link2, Loader2 } from "lucide-react";

const CreateNewShorten = ({ setOpen, refetch }) => {
  const { token } = useStoreContext();
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    defaultValues: { originalUrl: "" },
    mode: "onTouched",
  });

  const createShortUrlHandler = async (data) => {
    setLoading(true);
    try {
      const { data: res } = await api.post("/api/urls/shorten", data, {
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
          Authorization: "Bearer " + token,
        },
      });

      const shortenUrl = `${import.meta.env.VITE_REACT_FRONT_END_URL}/s/${res.shortUrl}`;
      try {
        await navigator.clipboard.writeText(shortenUrl);
        toast.success("Short URL created & copied! 🚀", {
          position: "bottom-center",
          duration: 3000,
        });
      } catch (e) {
        toast.success("Short URL created!");
      }

      await refetch();
      reset();
      setOpen(false);
    } catch (error) {
      toast.error("Failed to create short URL.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit(createShortUrlHandler)} className="space-y-5 mt-2">
      <div className="space-y-1.5">
        <label className="text-xs font-semibold text-white/50 uppercase tracking-wider">
          Original URL
        </label>
        <div className="relative">
          <Link2 className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-white/30" />
          <Input
            type="url"
            placeholder="https://example.com/your-very-long-url"
            className={`pl-10 ${errors.originalUrl ? "border-red-500/50" : ""}`}
            {...register("originalUrl", { required: "URL is required" })}
          />
        </div>
        {errors.originalUrl && (
          <p className="text-xs text-red-400">{errors.originalUrl.message}</p>
        )}
      </div>

      <Button
        type="submit"
        disabled={loading}
        variant="gradient"
        className="w-full shadow-lg shadow-violet-500/25"
      >
        {loading ? (
          <>
            <Loader2 className="w-4 h-4 animate-spin" />
            Creating...
          </>
        ) : (
          <>
            <Link2 className="w-4 h-4" />
            Create Short Link
          </>
        )}
      </Button>
    </form>
  );
};

export default CreateNewShorten;