import React from 'react'
import ShortenItem from './ShortenItem'

const ShortenUrlList = ({ data }) => {
  return (
    <div className="flex flex-col gap-4">
        {data.map((item) => (
            <ShortenItem key={item.id || item.shortUrl} {...item} />
        ))}
    </div>
  )
}

export default ShortenUrlList