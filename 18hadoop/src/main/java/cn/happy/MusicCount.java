package cn.happy.hadoop.mapreduce;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import cn.happy.hadoop.mapreduce.MusicCount.MusicMap.MusicReduce;

public class MusicCount {

	public static class MusicMap extends Mapper<Object, Text, Text, Text> {
		private Text musicName = new Text();
		private Text userName = new Text();

		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			StringTokenizer itr = new StringTokenizer(value.toString());
			while (itr.hasMoreTokens()) {
				String content = itr.nextToken();
				String[] splits = content.split(",");
				String name = splits[0]; // 姓名
				String music = splits[1]; // 歌曲
				userName.set(name);
				musicName.set(music);
				context.write(userName, musicName);
			}
		}

		public static class MusicReduce extends Reducer<Text, Text, Text, Text> {
			private Text userNames = new Text();

			@Override
			public void reduce(Text key, Iterable<Text> values, Context context)
					throws IOException, InterruptedException {
				StringBuffer result = new StringBuffer();
				int i = 0;
				for (Text tempText : values) {
					result.append("歌曲" + i + ":" + tempText.toString() + "\t");
					i++;
				}
				userNames.set(result.toString());
				context.write(key, userNames);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length != 2) {
			System.err.println("Usage: MinMaxCountDriver <in> <out>");
			System.exit(2);
		}
		Job job = new Job(conf, "StackOverflow Comment Date Min Max Count");
		job.setJarByClass(MusicCount.class);
		job.setMapperClass(MusicMap.class);
		job.setReducerClass(MusicReduce.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}
}