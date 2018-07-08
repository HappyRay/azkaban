#! /usr/bin/env python3
import subprocess


def update_tags():
    """
    Update the tags in the local branch from the remote.
    """
    pass


def get_latest_tag():
    cmd = 'git describe --abbrev=0'
    output = subprocess.run(cmd, capture_output=True, check=True, text=True, shell=True)
    latest_tag = output.stdout
    print("Latest tag : {}".format(latest_tag))
    return latest_tag


def calculate_new_version():
    latest_tag = get_latest_tag()
    pass


def publish_new_version(new_version):
    pass


def create_release():
    update_tags()
    new_version = calculate_new_version()
    publish_new_version(new_version)


if __name__ == "__main__":
    create_release()
